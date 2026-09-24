const { db, ErrorCodes } = require('../shared');

async function sendNotification({ uid, title, message, type, deepLink }) {
  // Strip any medical details from push payloads
  const sanitizedTitle = title.replace(/medical|prescription|diagnosis/gi, 'Health Notification');

  const ref = await db.collection('notifications').add({
    uid,
    title: sanitizedTitle,
    message,
    type: type || 'SYSTEM',
    deep_link: deepLink || null,
    is_read: false,
    created_at: new Date()
  });

  return ref.id;
}

module.exports = {
  sendNotification
};
