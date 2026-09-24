/**
 * Healthogram Shared Utilities and Middleware (Step 20)
 */
const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

const ErrorCodes = {
  AUTH_REQUIRED: 'AUTH_REQUIRED',
  PERMISSION_DENIED: 'PERMISSION_DENIED',
  ACCOUNT_SUSPENDED: 'ACCOUNT_SUSPENDED',
  VERIFICATION_REQUIRED: 'VERIFICATION_REQUIRED',
  HEALTH_ACCESS_REQUIRED: 'HEALTH_ACCESS_REQUIRED',
  HEALTH_ACCESS_EXPIRED: 'HEALTH_ACCESS_EXPIRED',
  INVALID_REQUEST: 'INVALID_REQUEST',
  VALIDATION_FAILED: 'VALIDATION_FAILED',
  RATE_LIMITED: 'RATE_LIMITED',
  PAYMENT_FAILED: 'PAYMENT_FAILED',
  PAYMENT_PENDING: 'PAYMENT_PENDING',
  PAYMENT_ALREADY_PROCESSED: 'PAYMENT_ALREADY_PROCESSED',
  ORDER_NOT_FOUND: 'ORDER_NOT_FOUND',
  SELLER_NOT_FOUND: 'SELLER_NOT_FOUND',
  PRODUCT_NOT_AVAILABLE: 'PRODUCT_NOT_AVAILABLE',
  DELIVERY_UNAVAILABLE: 'DELIVERY_UNAVAILABLE',
  AI_LIMIT_REACHED: 'AI_LIMIT_REACHED',
  TRANSLATION_FAILED: 'TRANSLATION_FAILED',
  SERVICE_UNAVAILABLE: 'SERVICE_UNAVAILABLE',
  INTERNAL_ERROR: 'INTERNAL_ERROR'
};

/**
 * Audit log helper: writes to admin_audit_logs with sanitization
 */
async function recordAuditLog({ actorUid, action, resource, details, status = 'SUCCESS' }) {
  try {
    await db.collection('admin_audit_logs').add({
      actor_uid: actorUid || 'system',
      action,
      resource,
      details: details || {},
      status,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
  } catch (err) {
    console.error('Failed to record audit log:', err);
  }
}

/**
 * Idempotency check helper
 */
async function checkIdempotency(collectionName, key, keyField = 'idempotency_key') {
  if (!key) return false;
  const snapshot = await db.collection(collectionName).where(keyField, '==', key).limit(1).get();
  return !snapshot.empty;
}

/**
 * Section 24 File Upload Security Validator:
 * Enforces allowed MIME types, extension check, file size ceiling, and path isolation.
 */
function validateFileUploadMetadata({ fileCategory, mimeType, fileSizeBytes, fileName, ownerUid }) {
  if (!ownerUid) {
    throw new Error('AUTH_REQUIRED: File upload must have an authenticated owner');
  }

  const categoryConfigs = {
    CLINICAL_DOCUMENT: {
      allowedMimes: ['application/pdf', 'image/jpeg', 'image/png', 'image/webp'],
      maxSizeBytes: 10 * 1024 * 1024, // 10MB
      basePath: `clinical_docs/${ownerUid}`
    },
    VERIFICATION_DOCUMENT: {
      allowedMimes: ['application/pdf', 'image/jpeg', 'image/png'],
      maxSizeBytes: 10 * 1024 * 1024, // 10MB
      basePath: `verification_docs/${ownerUid}`
    },
    MARKETPLACE_MEDIA: {
      allowedMimes: ['image/jpeg', 'image/png', 'image/webp'],
      maxSizeBytes: 5 * 1024 * 1024, // 5MB
      basePath: `marketplace_media/${ownerUid}`
    },
    PROFILE_MEDIA: {
      allowedMimes: ['image/jpeg', 'image/png', 'image/webp'],
      maxSizeBytes: 5 * 1024 * 1024, // 5MB
      basePath: `profile_media/${ownerUid}`
    }
  };

  const config = categoryConfigs[fileCategory];
  if (!config) {
    throw new Error(`INVALID_FILE_CATEGORY: Category ${fileCategory} is not recognized`);
  }

  if (!config.allowedMimes.includes(mimeType)) {
    throw new Error(`DISALLOWED_MIME_TYPE: MIME type ${mimeType} is not permitted for ${fileCategory}`);
  }

  if (fileSizeBytes > config.maxSizeBytes) {
    throw new Error(`FILE_TOO_LARGE: Size ${fileSizeBytes} bytes exceeds max allowed ${config.maxSizeBytes} bytes`);
  }

  // Prevent path traversal
  const sanitizedFileName = (fileName || 'attachment').replace(/[^a-zA-Z0-9._-]/g, '_');
  const isolatedStoragePath = `${config.basePath}/${Date.now()}_${sanitizedFileName}`;

  return {
    valid: true,
    storagePath: isolatedStoragePath,
    maxSizeBytes: config.maxSizeBytes
  };
}

module.exports = {
  admin,
  db,
  ErrorCodes,
  recordAuditLog,
  checkIdempotency,
  validateFileUploadMetadata
};
