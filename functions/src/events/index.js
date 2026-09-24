/**
 * Healthogram 2.0 Domain Event Processing Engine
 * Implements ADR-012: Durable Domain Event Architecture with Idempotency.
 */
const { db, ErrorCodes, recordAuditLog } = require('../shared');

/**
 * Ingest and process a domain event with at-least-once delivery and strict idempotency.
 */
async function processDomainEvent(data, context) {
  const {
    eventId,
    eventType,
    aggregateType,
    aggregateId,
    actorUid,
    idempotencyKey,
    payloadReference,
    metadata
  } = data;

  if (!eventId || !eventType || !idempotencyKey) {
    throw new Error(ErrorCodes.VALIDATION_FAILED + ': Missing eventId, eventType, or idempotencyKey');
  }

  // Atomically check and claim idempotency key
  const processedEventRef = db.collection('processed_events').doc(eventId);

  const isDuplicate = await db.runTransaction(async (transaction) => {
    const doc = await transaction.get(processedEventRef);
    if (doc.exists) {
      return true; // Already processed
    }

    transaction.set(processedEventRef, {
      event_id: eventId,
      event_type: eventType,
      aggregate_type: aggregateType || 'unknown',
      aggregate_id: aggregateId || 'unknown',
      actor_uid: actorUid || 'system',
      idempotency_key: idempotencyKey,
      processed_at: new Date(),
      status: 'PROCESSED'
    });
    return false;
  });

  if (isDuplicate) {
    return { status: 'SKIPPED_DUPLICATE', eventId };
  }

  // Audit domain event
  await recordAuditLog({
    actorUid: actorUid || 'system',
    action: `DOMAIN_EVENT_${eventType.toUpperCase().replace(/\./g, '_')}`,
    resource: `${aggregateType}/${aggregateId}`,
    details: { eventId, idempotencyKey }
  });

  return { status: 'PROCESSED', eventId };
}

module.exports = {
  processDomainEvent
};
