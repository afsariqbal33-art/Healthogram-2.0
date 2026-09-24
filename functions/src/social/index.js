const { db, ErrorCodes } = require('../shared');

async function togglePostLike(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const uid = context.auth.uid;
  const { postId } = data;

  const likeId = `${postId}_${uid}`;
  const likeRef = db.collection('post_likes').doc(likeId);
  const postRef = db.collection('posts').doc(postId);

  const doc = await likeRef.get();
  if (doc.exists) {
    await likeRef.delete();
    await postRef.update({
      like_count: db.FieldValue.increment(-1)
    });
    return { liked: false };
  } else {
    await likeRef.set({
      post_id: postId,
      uid,
      created_at: new Date()
    });
    await postRef.update({
      like_count: db.FieldValue.increment(1)
    });
    return { liked: true };
  }
}

module.exports = {
  togglePostLike
};
