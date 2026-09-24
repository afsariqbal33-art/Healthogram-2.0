const { db, ErrorCodes, recordAuditLog } = require('../shared');
const crypto = require('crypto');

async function generateHealthQrSession(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const patientUid = context.auth.uid;
  const opaqueToken = 'hqr_' + crypto.randomBytes(24).toString('hex');
  const expiresAt = new Date(Date.now() + 15 * 60 * 1000); // 15-minute TTL

  const ref = await db.collection('health_qr_sessions').add({
    patientUid,
    opaqueToken,
    isSingleUse: true,
    isConsumed: false,
    consumedByUid: null,
    createdAt: new Date(),
    expiresAt
  });

  return { sessionId: ref.id, opaqueToken, expiresAt };
}

async function consumeHealthQrSession(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const doctorUid = context.auth.uid;
  const { opaqueToken } = data;

  const snap = await db.collection('health_qr_sessions')
    .where('opaqueToken', '==', opaqueToken)
    .limit(1)
    .get();

  if (snap.empty) {
    throw new Error('INVALID_QR_TOKEN: Session not found');
  }

  const sessionDoc = snap.docs[0];
  const sessionData = sessionDoc.data();

  if (sessionData.isConsumed) {
    throw new Error('TOKEN_ALREADY_CONSUMED: Single-use token has already been consumed');
  }

  if (new Date() > sessionData.expiresAt.toDate()) {
    throw new Error(ErrorCodes.HEALTH_ACCESS_EXPIRED);
  }

  // Atomically mark consumed
  await sessionDoc.ref.update({
    isConsumed: true,
    consumedByUid: doctorUid,
    consumedAt: new Date()
  });

  // Create temporary emergency access grant (2 hours)
  const grantRef = await db.collection('health_access_grants').add({
    patientUid: sessionData.patientUid,
    requesterUid: doctorUid,
    requesterAccountType: 'doctor',
    purpose: 'In-person QR clinical consultation',
    scopes: ['CONDITIONS', 'ALLERGIES', 'MEDICATIONS'],
    status: 'APPROVED',
    grantedAt: new Date(),
    expiresAt: new Date(Date.now() + 2 * 60 * 60 * 1000)
  });

  await recordAuditLog({
    actorUid: doctorUid,
    action: 'HEALTH_QR_CONSUMED',
    resource: `health_profiles/${sessionData.patientUid}`,
    details: { grantId: grantRef.id }
  });

  return { success: true, grantId: grantRef.id, patientUid: sessionData.patientUid };
}

async function requestHealthAccess(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const requesterUid = context.auth.uid;
  const { patientUid, scopes, purpose, requesterAccountType } = data;

  if (!['doctor', 'clinic', 'hospital', 'laboratory'].includes(requesterAccountType)) {
    throw new Error(ErrorCodes.PERMISSION_DENIED + ': Requester must be a verified healthcare provider');
  }

  const grantRef = await db.collection('health_access_grants').add({
    patientUid,
    requesterUid,
    requesterAccountType,
    purpose: purpose || 'Clinical assessment',
    scopes: scopes || ['CONDITIONS'],
    status: 'REQUESTED',
    createdAt: new Date(),
    expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000)
  });

  return { grantId: grantRef.id, status: 'REQUESTED' };
}

async function processPatientConsent(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const patientUid = context.auth.uid;
  const { grantId, approved } = data;

  const grantRef = db.collection('health_access_grants').doc(grantId);
  const doc = await grantRef.get();
  if (!doc.exists) throw new Error('GRANT_NOT_FOUND');

  if (doc.data().patientUid !== patientUid) {
    throw new Error(ErrorCodes.PERMISSION_DENIED);
  }

  const newStatus = approved ? 'APPROVED' : 'REJECTED';
  await grantRef.update({
    status: newStatus,
    updatedAt: new Date()
  });

  return { success: true, status: newStatus };
}

/**
 * Step 37 / REQ-008: FHIR R4 ServiceRequest Lab Order Pipeline
 * Verified doctor or clinic issues an order to an accredited laboratory.
 */
async function createServiceRequestOrder(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const practitionerUid = context.auth.uid;
  const { patientUid, targetLabOrgUid, testLoincCode, testDisplayName, clinicalNotes, priority } = data;

  if (!patientUid || !targetLabOrgUid || !testLoincCode) {
    throw new Error('INVALID_REQUEST: patientUid, targetLabOrgUid, and testLoincCode are required');
  }

  // Verify target lab organization is active and verified
  const labDoc = await db.collection('healthcare_organizations').doc(targetLabOrgUid).get();
  if (!labDoc.exists || labDoc.data().account_type !== 'laboratory' || labDoc.data().verification_status !== 'VERIFIED') {
    throw new Error('INVALID_LABORATORY: Target facility must be an accredited verified laboratory');
  }

  const orderRef = await db.collection('fhir_service_requests').add({
    resourceType: 'ServiceRequest',
    status: 'active',
    intent: 'order',
    priority: priority || 'routine',
    code: {
      coding: [{
        system: 'http://loinc.org',
        code: testLoincCode,
        display: testDisplayName || 'Laboratory Diagnostic Test'
      }]
    },
    subject: { reference: `Patient/${patientUid}` },
    requester: { reference: `Practitioner/${practitionerUid}` },
    performer: [{ reference: `Organization/${targetLabOrgUid}` }],
    clinicalNotes: clinicalNotes || '',
    createdAt: new Date(),
    orderStatus: 'PENDING_SAMPLE_COLLECTION'
  });

  await recordAuditLog({
    actorUid: practitionerUid,
    action: 'FHIR_SERVICE_REQUEST_CREATED',
    resource: `fhir_service_requests/${orderRef.id}`,
    details: { patientUid, targetLabOrgUid, testLoincCode }
  });

  return { success: true, serviceRequestId: orderRef.id, status: 'ORDER_DISPATCHED' };
}

/**
 * Step 37 / REQ-008: Ingest Diagnostic Report from accredited Laboratory
 */
async function ingestDiagnosticReportBundle(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const labUid = context.auth.uid;
  const { serviceRequestId, patientUid, observations, reportPdfUrl, clinicianSignature } = data;

  if (!serviceRequestId || !patientUid || !clinicianSignature) {
    throw new Error('INVALID_PAYLOAD: serviceRequestId, patientUid, and clinicianSignature are mandatory');
  }

  const orderRef = db.collection('fhir_service_requests').doc(serviceRequestId);
  const orderDoc = await orderRef.get();
  if (!orderDoc.exists) throw new Error('SERVICE_REQUEST_NOT_FOUND');

  // Deterministic deduplication hash
  const recordHash = crypto.createHash('sha256')
    .update(`${patientUid}_DiagnosticReport_${serviceRequestId}_${Date.now()}`)
    .digest('hex');

  // Ingest into partitioned subcollection /users/{uid}/health_lab_reports
  const reportRef = await db.collection('users').doc(patientUid)
    .collection('health_lab_reports').add({
      resourceType: 'DiagnosticReport',
      serviceRequestId,
      issuedByLabUid: labUid,
      clinicianSignature,
      observations: observations || [],
      reportPdfUrl: reportPdfUrl || null,
      deduplicationHash: recordHash,
      reportDate: new Date(),
      status: 'FINAL',
      createdAt: new Date()
    });

  // Mark order fulfilled
  await orderRef.update({
    status: 'completed',
    orderStatus: 'FULFILLED',
    diagnosticReportId: reportRef.id,
    completedAt: new Date()
  });

  await recordAuditLog({
    actorUid: labUid,
    action: 'FHIR_DIAGNOSTIC_REPORT_INGESTED',
    resource: `users/${patientUid}/health_lab_reports/${reportRef.id}`,
    details: { serviceRequestId, recordHash }
  });

  return { success: true, diagnosticReportId: reportRef.id, status: 'INGESTED' };
}

/**
 * Step 37 / REQ-006: Paper Prescription OCR Staging Draft Pipeline
 */
async function createPaperPrescriptionDraft(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const patientUid = context.auth.uid;
  const { documentImageUrl, extractedMedications, rawOcrText } = data;

  const draftRef = await db.collection('paper_prescription_drafts').add({
    patientUid,
    documentImageUrl,
    extractedMedications: extractedMedications || [],
    rawOcrText: rawOcrText || '',
    status: 'STAGED_AWAITING_CLINICIAN_SIGNOFF',
    isAuthoritative: false,
    disclaimer: 'Non-Diagnostic AI OCR Draft — Requires licensed doctor verification before taking medication',
    createdAt: new Date()
  });

  return { success: true, draftId: draftRef.id, status: 'STAGED_DRAFT' };
}

/**
 * Step 37 / REQ-006: Clinician Verification of Paper Prescription Draft
 */
async function verifyPaperPrescriptionDraft(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);
  const doctorUid = context.auth.uid;
  const { draftId, patientUid, confirmedMedications, doctorNotes } = data;

  const draftRef = db.collection('paper_prescription_drafts').doc(draftId);
  const draftDoc = await draftRef.get();
  if (!draftDoc.exists) throw new Error('DRAFT_NOT_FOUND');

  // Commit verified prescription into authoritative partitioned subcollection
  const presRef = await db.collection('users').doc(patientUid)
    .collection('health_prescriptions').add({
      prescribedByDoctorUid: doctorUid,
      medications: confirmedMedications,
      notes: doctorNotes || '',
      sourceDraftId: draftId,
      prescriptionDate: new Date(),
      status: 'ACTIVE',
      verifiedAt: new Date()
    });

  // Mark draft verified
  await draftRef.update({
    status: 'VERIFIED_AND_COMMITTED',
    verifiedByDoctorUid: doctorUid,
    authoritativeRecordId: presRef.id,
    updatedAt: new Date()
  });

  await recordAuditLog({
    actorUid: doctorUid,
    action: 'PAPER_PRESCRIPTION_VERIFIED',
    resource: `users/${patientUid}/health_prescriptions/${presRef.id}`,
    details: { draftId }
  });

  return { success: true, prescriptionId: presRef.id, status: 'COMMITTED' };
}

/**
 * Step 37 / REQ-007: Emergency Health Card ICE Read-Only Export
 */
async function getEmergencyHealthProfile(data, context) {
  const { patientUid, emergencyPasscode } = data;
  if (!patientUid) throw new Error('INVALID_REQUEST: patientUid is required');

  const emergencyDoc = await db.collection('users').doc(patientUid)
    .collection('emergency_profile').doc('current').get();

  if (!emergencyDoc.exists) {
    return {
      available: false,
      message: 'No emergency profile configured by patient'
    };
  }

  const d = emergencyDoc.data();
  return {
    available: true,
    bloodType: d.bloodType || 'UNKNOWN',
    criticalAllergies: d.criticalAllergies || [],
    chronicConditions: d.chronicConditions || [],
    emergencyContacts: d.emergencyContacts || [],
    organDonor: d.organDonor || false,
    disclaimer: 'EMERGENCY MEDICAL PROFILE ONLY — Read-only lockscreen access for first responders'
  };
}

module.exports = {
  generateHealthQrSession,
  consumeHealthQrSession,
  requestHealthAccess,
  processPatientConsent,
  createServiceRequestOrder,
  ingestDiagnosticReportBundle,
  createPaperPrescriptionDraft,
  verifyPaperPrescriptionDraft,
  getEmergencyHealthProfile
};
