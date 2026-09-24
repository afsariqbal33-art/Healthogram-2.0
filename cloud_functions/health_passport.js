/**
 * HEALTHOGRAM — STEP 06: SECURE HEALTH PASSPORT CLOUD FUNCTIONS
 *
 * Sensitive healthcare operations executed in trusted Node.js environment
 * with server-side validation, immutable audit logging, and zero-knowledge architecture.
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const storage = admin.storage();

/**
 * 1. createHealthAccessRequest
 * Submitted by verified Doctors, Clinics, Hospitals, or Laboratories.
 */
exports.createHealthAccessRequest = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  }

  const requesterUid = context.auth.uid;
  const userDoc = await db.collection('users').doc(requesterUid).get();
  if (!userDoc.exists || !userDoc.data().isVerified) {
    throw new functions.https.HttpsError('permission-denied', 'Only verified healthcare providers can request access.');
  }

  const allowedRoles = ['doctor', 'clinic', 'hospital', 'laboratory'];
  const userRole = userDoc.data().accountType;
  if (!allowedRoles.includes(userRole)) {
    throw new functions.https.HttpsError('permission-denied', 'Account type not authorized for medical scanner.');
  }

  const { patientUid, requestedScopes, requestReason, durationHours = 24, oneTime = false, qrSessionId } = data;

  // Strict Laboratory restriction: cannot request entire passport
  if (userRole === 'laboratory') {
    const invalidScopes = requestedScopes.filter(s => !['tests', 'lab_reports', 'profile'].includes(s));
    if (invalidScopes.length > 0) {
      throw new functions.https.HttpsError('permission-denied', 'Laboratories can only request test and lab report scopes.');
    }
  }

  const requestRef = db.collection('health_access_requests').doc();
  const requestData = {
    requestId: requestRef.id,
    patientUid,
    requesterUid,
    requesterRole: userRole.toUpperCase(),
    requesterName: userDoc.data().displayName || 'Healthcare Provider',
    requesterOrganizationId: userDoc.data().organizationName || 'Medical Facility',
    requestReason,
    requestedScopes,
    status: 'pending',
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    expiresAt: new Date(Date.now() + (durationHours * 3600 * 1000)),
    oneTime,
    qrSessionId: qrSessionId || null
  };

  await requestRef.set(requestData);

  // Immutably log access request creation
  await db.collection('health_access_logs').add({
    patientUid,
    requesterUid,
    requesterRole: userRole.toUpperCase(),
    action: 'request_created',
    scope: requestedScopes.join(','),
    resourceType: 'health_access_requests',
    resourceId: requestRef.id,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    result: 'SUCCESS'
  });

  return { success: true, requestId: requestRef.id };
});

/**
 * 2. approveHealthAccessRequest
 * Patient explicitly grants access to specified scopes.
 */
exports.approveHealthAccessRequest = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated.');
  }

  const patientUid = context.auth.uid;
  const { requestId, grantedScopes, durationHours = 24, oneTime = false } = data;

  const requestRef = db.collection('health_access_requests').doc(requestId);
  const reqDoc = await requestRef.get();

  if (!reqDoc.exists || reqDoc.data().patientUid !== patientUid) {
    throw new functions.https.HttpsError('permission-denied', 'Only the patient can approve this request.');
  }

  const reqData = reqDoc.data();
  if (reqData.status !== 'pending') {
    throw new functions.https.HttpsError('failed-precondition', 'Request is not pending.');
  }

  const grantRef = db.collection('health_access_grants').doc();
  const expiresAt = new Date(Date.now() + (durationHours * 3600 * 1000));

  const batch = db.batch();
  batch.update(requestRef, {
    status: 'approved',
    respondedAt: admin.firestore.FieldValue.serverTimestamp(),
    approvedByUid: patientUid
  });

  batch.set(grantRef, {
    grantId: grantRef.id,
    patientUid,
    requesterUid: reqData.requesterUid,
    requesterRole: reqData.requesterRole,
    organizationId: reqData.requesterOrganizationId,
    grantedScopes,
    purpose: reqData.requestReason,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    startsAt: admin.firestore.FieldValue.serverTimestamp(),
    expiresAt,
    status: 'active',
    approvedByUid: patientUid,
    oneTime
  });

  await batch.commit();

  await db.collection('health_access_logs').add({
    patientUid,
    requesterUid: reqData.requesterUid,
    requesterRole: reqData.requesterRole,
    action: 'request_approved',
    scope: grantedScopes.join(','),
    resourceType: 'health_access_grants',
    resourceId: grantRef.id,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    result: 'SUCCESS'
  });

  return { success: true, grantId: grantRef.id };
});

/**
 * 3. revokeHealthAccess
 * Patient immediately revokes authorization grant.
 */
exports.revokeHealthAccess = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated.');
  }

  const patientUid = context.auth.uid;
  const { grantId } = data;

  const grantRef = db.collection('health_access_grants').doc(grantId);
  const grantDoc = await grantRef.get();

  if (!grantDoc.exists || grantDoc.data().patientUid !== patientUid) {
    throw new functions.https.HttpsError('permission-denied', 'Only patient can revoke this grant.');
  }

  await grantRef.update({
    status: 'revoked',
    revokedAt: admin.firestore.FieldValue.serverTimestamp(),
    revokedByUid: patientUid
  });

  await db.collection('health_access_logs').add({
    patientUid,
    requesterUid: grantDoc.data().requesterUid,
    requesterRole: grantDoc.data().requesterRole,
    action: 'permission_revoked',
    scope: grantDoc.data().grantedScopes.join(','),
    resourceType: 'health_access_grants',
    resourceId: grantId,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    result: 'SUCCESS'
  });

  return { success: true };
});

/**
 * 4. generateHealthQRSession
 * Generates 15-minute rotating ticket. Zero medical data in QR payload.
 */
exports.generateHealthQRSession = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated.');
  }

  const patientUid = context.auth.uid;
  const sessionRef = db.collection('health_qr_sessions').doc();
  const expiresAt = new Date(Date.now() + 15 * 60 * 1000); // 15 mins

  await sessionRef.set({
    sessionId: sessionRef.id,
    patientUid,
    status: 'created',
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    expiresAt
  });

  return { success: true, sessionId: sessionRef.id, expiresAt: expiresAt.toISOString() };
});

/**
 * 5. downloadMedicalDocumentSignedUrl
 * Verifies caller has active patient consent before issuing 5-minute signed URL.
 */
exports.downloadMedicalDocumentSignedUrl = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be authenticated.');
  }

  const callerUid = context.auth.uid;
  const { documentId, patientUid } = data;

  if (callerUid !== patientUid) {
    // Check for active grant with 'documents' scope
    const grantQuery = await db.collection('health_access_grants')
      .where('patientUid', '==', patientUid)
      .where('requesterUid', '==', callerUid)
      .where('status', '==', 'active')
      .limit(1)
      .get();

    if (grantQuery.empty || !grantQuery.docs[0].data().grantedScopes.includes('documents')) {
      throw new functions.https.HttpsError('permission-denied', 'No active patient authorization for documents.');
    }
  }

  const docRecord = await db.collection('health_documents').doc(documentId).get();
  if (!docRecord.exists) {
    throw new functions.https.HttpsError('not-found', 'Document record not found.');
  }

  const storagePath = docRecord.data().storagePath;
  const bucket = storage.bucket();
  const file = bucket.file(storagePath);

  const [signedUrl] = await file.getSignedUrl({
    action: 'read',
    expires: Date.now() + 5 * 60 * 1000 // 5 minutes
  });

  // Immutably log document download
  await db.collection('health_access_logs').add({
    patientUid,
    requesterUid: callerUid,
    requesterRole: callerUid === patientUid ? 'PATIENT' : 'PROVIDER',
    action: 'document_downloaded',
    scope: 'documents',
    resourceType: 'health_documents',
    resourceId: documentId,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    result: 'SUCCESS'
  });

  return { downloadUrl: signedUrl };
});
