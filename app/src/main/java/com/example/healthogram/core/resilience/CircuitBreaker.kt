package com.example.healthogram.core.resilience

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Healthogram 2.0 Circuit Breaker & Provider Fallback Engine.
 *
 * Implements Section 73 & 74:
 * - States: CLOSED (normal), OPEN (tripped due to high error rate), HALF_OPEN (trial recovery).
 * - Protects third-party providers (AI, Translation, Payments, Delivery, RTC, Search).
 * - Automatic recovery timeout and graceful degradation fallback.
 */
enum class CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}

class CircuitBreaker(
    val providerName: String,
    val failureThreshold: Int = 5,
    val recoveryTimeoutMillis: Long = 30_000L,
    val halfOpenSuccessThreshold: Int = 2
) {
    private val state = AtomicReference(CircuitState.CLOSED)
    private val consecutiveFailures = AtomicInteger(0)
    private val halfOpenSuccesses = AtomicInteger(0)
    private val lastStateChangeMillis = AtomicLong(System.currentTimeMillis())

    fun currentState(): CircuitState {
        val current = state.get()
        if (current == CircuitState.OPEN) {
            val elapsed = System.currentTimeMillis() - lastStateChangeMillis.get()
            if (elapsed >= recoveryTimeoutMillis) {
                if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                    lastStateChangeMillis.set(System.currentTimeMillis())
                    halfOpenSuccesses.set(0)
                    return CircuitState.HALF_OPEN
                }
            }
        }
        return state.get()
    }

    fun allowExecution(): Boolean {
        return currentState() != CircuitState.OPEN
    }

    fun recordSuccess() {
        when (currentState()) {
            CircuitState.HALF_OPEN -> {
                if (halfOpenSuccesses.incrementAndGet() >= halfOpenSuccessThreshold) {
                    state.set(CircuitState.CLOSED)
                    consecutiveFailures.set(0)
                    lastStateChangeMillis.set(System.currentTimeMillis())
                }
            }
            CircuitState.CLOSED -> {
                consecutiveFailures.set(0)
            }
            CircuitState.OPEN -> Unit
        }
    }

    fun recordFailure() {
        when (currentState()) {
            CircuitState.HALF_OPEN -> {
                // Instantly re-trip to OPEN if it fails during probe
                state.set(CircuitState.OPEN)
                lastStateChangeMillis.set(System.currentTimeMillis())
            }
            CircuitState.CLOSED -> {
                if (consecutiveFailures.incrementAndGet() >= failureThreshold) {
                    state.set(CircuitState.OPEN)
                    lastStateChangeMillis.set(System.currentTimeMillis())
                }
            }
            CircuitState.OPEN -> Unit
        }
    }

    fun forceOpen() {
        state.set(CircuitState.OPEN)
        lastStateChangeMillis.set(System.currentTimeMillis())
    }

    fun forceClose() {
        state.set(CircuitState.CLOSED)
        consecutiveFailures.set(0)
        halfOpenSuccesses.set(0)
        lastStateChangeMillis.set(System.currentTimeMillis())
    }

    /**
     * Executes a supplier with circuit protection and graceful fallback.
     */
    inline fun <T> executeWithFallback(
        action: () -> T,
        fallback: (Throwable?) -> T
    ): T {
        if (!allowExecution()) {
            return fallback(IllegalStateException("Circuit breaker for $providerName is OPEN"))
        }

        return try {
            val result = action()
            recordSuccess()
            result
        } catch (t: Throwable) {
            recordFailure()
            fallback(t)
        }
    }
}
