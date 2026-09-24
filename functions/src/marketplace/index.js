const { db, ErrorCodes, checkIdempotency, recordAuditLog } = require('../shared');

async function createMarketplaceOrder(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const customerUid = context.auth.uid;
  const { cartItems, shippingAddress, idempotencyKey } = data;

  if (await checkIdempotency('marketplace_orders', idempotencyKey)) {
    throw new Error(ErrorCodes.PAYMENT_ALREADY_PROCESSED);
  }

  // Group items by seller
  const sellerGroups = {};
  let grandTotal = 0;

  for (const item of cartItems) {
    if (!sellerGroups[item.seller_uid]) {
      sellerGroups[item.seller_uid] = [];
    }
    sellerGroups[item.seller_uid].push(item);
    grandTotal += item.price * item.quantity;
  }

  const batch = db.batch();
  const orderRef = db.collection('marketplace_orders').doc();
  const suborderIds = [];

  for (const [sellerUid, items] of Object.entries(sellerGroups)) {
    const suborderRef = db.collection('order_suborders').doc();
    suborderIds.push(suborderRef.id);
    const subtotal = items.reduce((sum, i) => sum + (i.price * i.quantity), 0);
    const commission = Math.round(subtotal * 0.10); // 10% platform commission
    const sellerPayout = subtotal - commission;

    batch.set(suborderRef, {
      suborder_id: suborderRef.id,
      parent_order_id: orderRef.id,
      seller_uid: sellerUid,
      customer_uid: customerUid,
      items,
      subtotal_amount: subtotal,
      commission_amount: commission,
      seller_payout_amount: sellerPayout,
      status: 'PLACED',
      created_at: new Date()
    });
  }

  batch.set(orderRef, {
    order_id: orderRef.id,
    customer_uid: customerUid,
    total_amount: grandTotal,
    suborder_ids: suborderIds,
    payment_status: 'PENDING',
    fulfillment_status: 'UNFULFILLED',
    shipping_address: shippingAddress || {},
    idempotency_key: idempotencyKey || null,
    created_at: new Date()
  });

  await batch.commit();

  return {
    order_id: orderRef.id,
    suborder_count: suborderIds.length,
    total_amount: grandTotal
  };
}

module.exports = {
  createMarketplaceOrder
};
