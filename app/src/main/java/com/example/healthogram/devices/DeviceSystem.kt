package com.example.healthogram.devices

import com.example.healthogram.core.AccountType
import java.util.UUID

/**
 * Granular device permission toggles for healthcare organizations.
 */
data class DevicePermissions(
    val organizationManagement: Boolean = true,
    val socialContent: Boolean = true,
    val marketplace: Boolean = true,
    val messages: Boolean = true,
    val calls: Boolean = true
)

enum class DeviceStatus {
    ACTIVE,
    REVOKED,
    SUSPENDED
}

/**
 * Registered active device entity.
 */
data class RegisteredDevice(
    val deviceId: String = UUID.randomUUID().toString(),
    val userId: String,
    val deviceName: String,
    val platform: String = "Android",
    val appVersion: String = "1.0.0",
    val status: DeviceStatus = DeviceStatus.ACTIVE,
    val permissions: DevicePermissions = DevicePermissions(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Subscription package tier defining device allowance.
 */
enum class SubscriptionPackage(val maxAllowedDevices: Int) {
    BASIC_ORGANIZATION(maxAllowedDevices = 4),
    PREMIUM_ORGANIZATION(maxAllowedDevices = 8),
    ENTERPRISE_ORGANIZATION(maxAllowedDevices = 32)
}

/**
 * Device Limit and Permission Enforcement Engine.
 */
class DeviceManager {

    companion object {
        const val NORMAL_ACCOUNT_DEVICE_LIMIT = 4
    }

    private val userDevices = mutableMapOf<String, MutableList<RegisteredDevice>>()

    /**
     * Resolves maximum allowed active devices for a given account and subscription package.
     */
    fun getMaxDevicesAllowed(accountType: AccountType, packageTier: SubscriptionPackage?): Int {
        return if (accountType.isHealthcareOrganization) {
            packageTier?.maxAllowedDevices ?: SubscriptionPackage.BASIC_ORGANIZATION.maxAllowedDevices
        } else {
            NORMAL_ACCOUNT_DEVICE_LIMIT
        }
    }

    /**
     * Registers a new device, ensuring device limit is never breached.
     */
    fun registerDevice(
        userId: String,
        accountType: AccountType,
        deviceName: String,
        packageTier: SubscriptionPackage? = null,
        initialPermissions: DevicePermissions = DevicePermissions()
    ): Result<RegisteredDevice> {
        val currentList = userDevices.getOrPut(userId) { mutableListOf() }
        val activeCount = currentList.count { it.status == DeviceStatus.ACTIVE }
        val maxAllowed = getMaxDevicesAllowed(accountType, packageTier)

        if (activeCount >= maxAllowed) {
            return Result.failure(
                IllegalStateException(
                    "Device Limit Exceeded: Your account tier allows maximum $maxAllowed active devices. Please revoke an existing device to proceed."
                )
            )
        }

        val newDevice = RegisteredDevice(
            userId = userId,
            deviceName = deviceName,
            permissions = initialPermissions
        )
        currentList.add(newDevice)
        return Result.success(newDevice)
    }

    /**
     * Healthcare Organization Admin updates permissions for a specific station/device.
     */
    fun updateDevicePermissions(
        userId: String,
        deviceId: String,
        newPermissions: DevicePermissions
    ): Result<RegisteredDevice> {
        val devices = userDevices[userId] ?: return Result.failure(NoSuchElementException("No devices for user"))
        val index = devices.indexOfFirst { it.deviceId == deviceId }
        if (index == -1) return Result.failure(NoSuchElementException("Device not found"))

        val updated = devices[index].copy(permissions = newPermissions)
        devices[index] = updated
        return Result.success(updated)
    }

    /**
     * Revokes a device remotely.
     */
    fun revokeDevice(userId: String, deviceId: String): Boolean {
        val devices = userDevices[userId] ?: return false
        val index = devices.indexOfFirst { it.deviceId == deviceId }
        if (index == -1) return false

        devices[index] = devices[index].copy(status = DeviceStatus.REVOKED)
        return true
    }

    fun getActiveDevices(userId: String): List<RegisteredDevice> {
        return userDevices[userId]?.filter { it.status == DeviceStatus.ACTIVE } ?: emptyList()
    }
}
