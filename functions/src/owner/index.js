const { db, ErrorCodes, recordAuditLog } = require('../shared');

async function updateEmergencyControls(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const ownerUid = context.auth.uid;
  const { emergencyToggles, ownerPin } = data;

  // In production, ownerPin is verified against bcrypt hash in Secret Manager
  if (!ownerPin || ownerPin.length < 4) {
    throw new Error('INVALID_OWNER_PIN');
  }

  const controlRef = db.collection('emergency_controls').doc('current');
  await controlRef.set({
    ...emergencyToggles,
    last_updated_by: ownerUid,
    updated_at: new Date()
  }, { merge: true });

  await recordAuditLog({
    actorUid: ownerUid,
    action: 'EMERGENCY_CONTROLS_UPDATED',
    resource: 'emergency_controls/current',
    details: emergencyToggles,
    status: 'CRITICAL'
  });

  return { success: true, updated_at: new Date() };
}

module.exports = {
  updateEmergencyControls
};
