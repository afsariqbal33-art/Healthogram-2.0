const { db, ErrorCodes } = require('../shared');

async function sendEncryptedMessage(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const senderUid = context.auth.uid;
  const { conversationId, text, mediaType, mediaUrl } = data;

  const convRef = db.collection('conversations').doc(conversationId);
  const convDoc = await convRef.get();
  if (!convDoc.exists) throw new Error('CONVERSATION_NOT_FOUND');

  if (!convDoc.data().participant_uids.includes(senderUid)) {
    throw new Error(ErrorCodes.PERMISSION_DENIED);
  }

  const msgRef = db.collection('messages').doc();
  await msgRef.set({
    message_id: msgRef.id,
    conversation_id: conversationId,
    sender_uid: senderUid,
    text,
    media_type: mediaType || 'TEXT',
    media_url: mediaUrl || null,
    delivery_status: 'SENT',
    created_at: new Date()
  });

  await convRef.update({
    last_message_text: text ? text.substring(0, 50) : '[Media]',
    last_message_sender_uid: senderUid,
    last_message_at: new Date()
  });

  return { success: true, messageId: msgRef.id };
}

module.exports = {
  sendEncryptedMessage
};
