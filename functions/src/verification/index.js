const { db, ErrorCodes, recordAuditLog } = require('../shared');

async function reviewVerificationApplication(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const reviewerUid = context.auth.uid;
  const { applicationId, decision, reason } = data;

  const appRef = db.collection('verification_applications').doc(applicationId);
  const doc = await appRef.get();
  if (!doc.exists) throw new Error('APPLICATION_NOT_FOUND');

  const appData = doc.data();
  const isApproved = decision === 'APPROVED';

  await appRef.update({
    status: decision,
    decision_reason: reason || null,
    reviewed_by: reviewerUid,
    reviewed_at: new Date()
  });

  if (isApproved) {
    // Elevate user verification badge
    await db.collection('users').doc(appData.uid).update({
      is_verified: true,
      verification_status: 'approved',
      updated_at: new Date()
    });
  }

  await recordAuditLog({
    actorUid: reviewerUid,
    action: `VERIFICATION_${decision}`,
    resource: `users/${appData.uid}`,
    details: { applicationId }
  });

  return { success: true, decision };
}

module.exports = {
  reviewVerificationApplication
};
