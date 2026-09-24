const { db, ErrorCodes, recordAuditLog } = require('../shared');

async function applyAccountAction(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const adminUid = context.auth.uid;
  const { targetUid, actionType, reason, durationDays } = data;

  const actionRef = await db.collection('account_actions').add({
    target_uid: targetUid,
    action_type: actionType, // WARNING, SUSPENSION, BAN
    reason,
    issued_by: adminUid,
    duration_days: durationDays || null,
    created_at: new Date()
  });

  if (actionType === 'SUSPENSION' || actionType === 'BAN') {
    await db.collection('users').doc(targetUid).update({
      account_status: actionType === 'BAN' ? 'banned' : 'suspended',
      updated_at: new Date()
    });
  }

  await recordAuditLog({
    actorUid: adminUid,
    action: `ADMIN_ACTION_${actionType}`,
    resource: `users/${targetUid}`,
    details: { reason }
  });

  return { success: true, actionId: actionRef.id };
}

module.exports = {
  applyAccountAction
};
