const { db, ErrorCodes } = require('../shared');

async function verifyDeliveryOtp(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const { shipmentId, otpCode } = data;

  const shipmentRef = db.collection('shipments').doc(shipmentId);
  const doc = await shipmentRef.get();
  if (!doc.exists) throw new Error('SHIPMENT_NOT_FOUND');

  // Verify against active delivery OTP session
  const otpSnap = await db.collection('delivery_otp_sessions')
    .where('shipmentId', '==', shipmentId)
    .where('otpCode', '==', otpCode)
    .limit(1)
    .get();

  if (otpSnap.empty) {
    throw new Error('INVALID_DELIVERY_OTP');
  }

  await shipmentRef.update({
    status: 'DELIVERED',
    delivered_at: new Date()
  });

  return { success: true, status: 'DELIVERED' };
}

module.exports = {
  verifyDeliveryOtp
};
