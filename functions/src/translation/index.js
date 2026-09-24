const { db, ErrorCodes } = require('../shared');

async function translateTextMessage(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const { messageId, text, targetLanguage } = data;

  const cacheKey = `${messageId}_${targetLanguage}`;
  const cacheRef = db.collection('message_translations').doc(cacheKey);
  const cacheDoc = await cacheRef.get();

  if (cacheDoc.exists) {
    return { translatedText: cacheDoc.data().translated_text, cached: true };
  }

  // Simulated high-fidelity translation provider
  const translatedText = `[${targetLanguage.toUpperCase()}] ` + text;

  await cacheRef.set({
    translation_id: cacheKey,
    message_id: messageId,
    target_language: targetLanguage,
    translated_text: translatedText,
    created_at: new Date()
  });

  return { translatedText, cached: false };
}

module.exports = {
  translateTextMessage
};
