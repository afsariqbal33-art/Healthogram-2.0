const { db, ErrorCodes, recordAuditLog } = require('../shared');

async function handlePaymentWebhook(req, res) {
  const event = req.body;
  const eventId = event.id;

  if (!eventId) {
    return res.status(400).json({ error: 'Missing event ID' });
  }

  // Deduplicate webhook event
  const eventRef = db.collection('payment_webhook_events').doc(eventId);
  const existingDoc = await eventRef.get();

  if (existingDoc.exists) {
    // Idempotent return - do not reprocess
    return res.status(200).json({ received: true, note: 'Duplicate webhook skipped' });
  }

  // Record webhook receipt
  await eventRef.set({
    event_id: eventId,
    event_type: event.type,
    received_at: new Date(),
    status: 'PROCESSED'
  });

  if (event.type === 'payment_intent.succeeded') {
    const paymentIntent = event.data.object;
    const orderId = paymentIntent.metadata.order_id;
    const amount = paymentIntent.amount;

    // Atomic transaction: update order, post ledger entries, credit seller balances
    await db.runTransaction(async (transaction) => {
      const orderRef = db.collection('marketplace_orders').doc(orderId);
      const orderDoc = await transaction.get(orderRef);

      if (!orderDoc.exists) return;

      // 1. Mark order paid
      transaction.update(orderRef, { payment_status: 'PAID', updated_at: new Date() });

      // 2. Post Master Escrow Credit
      const escrowLedgerRef = db.collection('financial_ledger_entries').doc();
      transaction.set(escrowLedgerRef, {
        ledger_entry_id: escrowLedgerRef.id,
        transaction_id: paymentIntent.id,
        entry_type: 'ESCROW_CREDIT',
        direction: 'CREDIT',
        amount,
        currency: paymentIntent.currency || 'USD',
        account_type: 'ESCROW',
        account_id: 'platform_escrow',
        status: 'POSTED',
        created_at: new Date()
      });

      // 3. Post Platform Commission
      const commissionAmount = Math.round(amount * 0.10);
      const commissionLedgerRef = db.collection('financial_ledger_entries').doc();
      transaction.set(commissionLedgerRef, {
        ledger_entry_id: commissionLedgerRef.id,
        transaction_id: paymentIntent.id,
        entry_type: 'PLATFORM_COMMISSION',
        direction: 'CREDIT',
        amount: commissionAmount,
        currency: paymentIntent.currency || 'USD',
        account_type: 'OWNER_REVENUE',
        account_id: 'platform_treasury',
        status: 'POSTED',
        created_at: new Date()
      });
    });

    await recordAuditLog({
      actorUid: 'stripe_webhook',
      action: 'PAYMENT_CAPTURED',
      resource: `marketplace_orders/${orderId}`,
      details: { amount, eventId }
    });
  }

  return res.status(200).json({ received: true });
}

module.exports = {
  handlePaymentWebhook
};
