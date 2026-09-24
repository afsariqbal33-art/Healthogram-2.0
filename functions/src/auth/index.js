const { db, ErrorCodes, recordAuditLog } = require('../shared');
const crypto = require('crypto');

async function validateUserSession(data, context) {
  if (!context.auth) {
    throw new Error(ErrorCodes.AUTH_REQUIRED);
  }
  const uid = context.auth.uid;
  const { deviceId } = data;

  const sessionsSnapshot = await db.collection('user_devices')
    .where('uid', '==', uid)
    .where('is_active', '==', true)
    .get();

  if (sessionsSnapshot.size > 4) {
    throw new Error('DEVICE_LIMIT_EXCEEDED: Maximum 4 active devices permitted');
  }

  return { valid: true, activeDevices: sessionsSnapshot.size };
}

async function registerDeviceSession(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const uid = context.auth.uid;
  const { deviceId, deviceModel, osVersion } = data;

  if (!deviceId) throw new Error(ErrorCodes.INVALID_REQUEST);

  const activeSessions = await db.collection('user_devices')
    .where('uid', '==', uid)
    .where('is_active', '==', true)
    .orderBy('last_active_at', 'asc')
    .get();

  // Check if current device is already an active session
  let existingDoc = null;
  activeSessions.docs.forEach(doc => {
    if (doc.data().device_id === deviceId) {
      existingDoc = doc;
    }
  });

  if (existingDoc) {
    await existingDoc.ref.update({
      last_active_at: new Date(),
      device_model: deviceModel || existingDoc.data().device_model,
      os_version: osVersion || existingDoc.data().os_version
    });
    return { success: true, activeDevices: activeSessions.size, status: 'SESSION_REFRESHED' };
  }

  // If 4 or more active devices exist, evict the oldest device
  if (activeSessions.size >= 4) {
    const oldestDoc = activeSessions.docs[0];
    await oldestDoc.ref.update({
      is_active: false,
      revoked_at: new Date(),
      revocation_reason: 'CONCURRENT_DEVICE_LIMIT_EVICTION'
    });
    await recordAuditLog({
      actorUid: uid,
      action: 'DEVICE_EVICTED_LIMIT_EXCEEDED',
      resource: `user_devices/${oldestDoc.id}`,
      details: { evictedDeviceId: oldestDoc.data().device_id, newDeviceId: deviceId }
    });
  }

  const newSession = await db.collection('user_devices').add({
    uid,
    device_id: deviceId,
    device_model: deviceModel || 'Android Device',
    os_version: osVersion || 'Android 16',
    is_active: true,
    created_at: new Date(),
    last_active_at: new Date()
  });

  return { success: true, sessionId: newSession.id, status: 'SESSION_REGISTERED' };
}

async function revokeDeviceSession(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const uid = context.auth.uid;
  const { deviceId } = data;

  const snapshot = await db.collection('user_devices')
    .where('uid', '==', uid)
    .where('device_id', '==', deviceId)
    .where('is_active', '==', true)
    .get();

  if (snapshot.empty) {
    throw new Error('DEVICE_NOT_FOUND: No active session found for this device');
  }

  for (const doc of snapshot.docs) {
    await doc.ref.update({
      is_active: false,
      revoked_at: new Date(),
      revocation_reason: 'USER_INITIATED_REVOCATION'
    });
  }

  await recordAuditLog({
    actorUid: uid,
    action: 'DEVICE_SESSION_REVOKED',
    resource: `user_devices/${deviceId}`,
    details: { deviceId }
  });

  return { success: true, message: 'Device session successfully revoked' };
}

async function getUserSessions(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const uid = context.auth.uid;

  const snapshot = await db.collection('user_devices')
    .where('uid', '==', uid)
    .where('is_active', '==', true)
    .get();

  const sessions = snapshot.docs.map(doc => {
    const d = doc.data();
    return {
      id: doc.id,
      deviceId: d.device_id,
      deviceModel: d.device_model,
      osVersion: d.os_version,
      lastActiveAt: d.last_active_at ? d.last_active_at.toDate() : null,
      createdAt: d.created_at ? d.created_at.toDate() : null
    };
  });

  return { sessions, totalActive: sessions.length };
}

async function requestAccountDeletion(data, context) {
  if (!context.auth) {
    throw new Error(ErrorCodes.AUTH_REQUIRED);
  }
  const uid = context.auth.uid;
  const { reason } = data;

  const docRef = await db.collection('account_deletion_requests').add({
    uid,
    reason: reason || 'User requested erasure',
    status: 'PENDING_GRACE_PERIOD',
    created_at: new Date(),
    scheduled_deletion_at: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
  });

  await recordAuditLog({
    actorUid: uid,
    action: 'ACCOUNT_DELETION_REQUESTED',
    resource: `users/${uid}`,
    details: { requestId: docRef.id }
  });

  return { success: true, requestId: docRef.id };
}

async function logoutAllDevices(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const uid = context.auth.uid;
  const { currentDeviceId } = data;

  const snapshot = await db.collection('user_devices')
    .where('uid', '==', uid)
    .where('is_active', '==', true)
    .get();

  const batch = db.batch();
  let revokedCount = 0;

  for (const doc of snapshot.docs) {
    // Optionally keep current device or revoke all
    const isCurrent = doc.data().device_id === currentDeviceId;
    batch.update(doc.ref, {
      is_active: false,
      revoked_at: new Date(),
      revocation_reason: isCurrent ? 'USER_LOGOUT' : 'LOGOUT_ALL_DEVICES'
    });
    revokedCount++;
  }

  await batch.commit();

  await recordAuditLog({
    actorUid: uid,
    action: 'LOGOUT_ALL_DEVICES',
    resource: `users/${uid}`,
    details: { revokedCount }
  });

  return { success: true, revokedCount };
}

async function reauthenticateForSensitiveAction(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const uid = context.auth.uid;
  const { actionType, challengeProof } = data;

  const allowedSensitiveActions = [
    'WITHDRAWAL',
    'UPDATE_PAYOUT_SETTINGS',
    'REVOKE_ALL_SESSIONS',
    'EXPORT_FULL_HEALTH_PASSPORT',
    'ACCOUNT_DELETION',
    'DISABLE_MFA'
  ];

  if (!allowedSensitiveActions.includes(actionType)) {
    throw new Error('INVALID_SENSITIVE_ACTION: Unrecognized sensitive action type');
  }

  if (!challengeProof) {
    throw new Error('CHALLENGE_PROOF_REQUIRED: User must provide biometric or password challenge proof');
  }

  const token = 'reauth_' + crypto.randomBytes(32).toString('hex');
  const expiresAt = new Date(Date.now() + 5 * 60 * 1000); // 5-minute TTL

  await db.collection('sensitive_action_tickets').add({
    uid,
    actionType,
    token,
    isConsumed: false,
    createdAt: new Date(),
    expiresAt
  });

  await recordAuditLog({
    actorUid: uid,
    action: 'REAUTH_CHALLENGE_ISSUED',
    resource: `users/${uid}`,
    details: { actionType }
  });

  return { success: true, reauthToken: token, expiresAt };
}

/**
 * Scheduled cron function (DEBT-05 remediation):
 * Purges inactive device sessions older than 90 days.
 */
async function purgeZombieSessions() {
  const ninetyDaysAgo = new Date(Date.now() - 90 * 24 * 60 * 60 * 1000);

  const snapshot = await db.collection('user_devices')
    .where('is_active', '==', true)
    .where('last_active_at', '<', ninetyDaysAgo)
    .limit(500)
    .get();

  if (snapshot.empty) {
    return { purgedCount: 0 };
  }

  const batch = db.batch();
  snapshot.docs.forEach(doc => {
    batch.update(doc.ref, {
      is_active: false,
      revoked_at: new Date(),
      revocation_reason: 'ZOMBIE_SESSION_SCHEDULED_PURGE'
    });
  });

  await batch.commit();

  await recordAuditLog({
    actorUid: 'SYSTEM_CRON',
    action: 'ZOMBIE_SESSIONS_PURGED',
    resource: 'user_devices',
    details: { purgedCount: snapshot.size }
  });

  return { purgedCount: snapshot.size };
}

module.exports = {
  validateUserSession,
  registerDeviceSession,
  revokeDeviceSession,
  getUserSessions,
  requestAccountDeletion,
  logoutAllDevices,
  reauthenticateForSensitiveAction,
  purgeZombieSessions
};
