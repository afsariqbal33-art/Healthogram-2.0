const { db, ErrorCodes } = require('../shared');

async function initiateCallSession(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const callerUid = context.auth.uid;
  const { receiverUid, callType } = data;

  const sessionRef = db.collection('call_sessions').doc();
  await sessionRef.set({
    call_id: sessionRef.id,
    caller_uid: callerUid,
    receiver_uid: receiverUid,
    call_type: callType || 'VOICE',
    status: 'RINGING',
    created_at: new Date()
  });

  return { callId: sessionRef.id, status: 'RINGING' };
}

module.exports = {
  initiateCallSession
};
