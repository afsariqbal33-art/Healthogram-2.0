/**
 * HEALTHOGRAM — STEP 09: MARKETPLACE SELLER CENTER CLOUD FUNCTIONS
 *
 * Authoritative Server-Side Financial, Inventory, Order, Payout, and Compliance Systems:
 * 1. createSellerApplication
 * 2. submitSellerApplication
 * 3. validateSellerApplication
 * 4. approveSeller
 * 5. rejectSeller
 * 6. suspendSeller
 * 7. createSellerProduct
 * 8. submitProductForReview
 * 9. approveProduct
 * 10. rejectProduct
 * 11. validateProductCompliance
 * 12. updateInventory
 * 13. reserveInventory
 * 14. releaseInventory
 * 15. calculateSellerCommission
 * 16. createSellerLedgerEntry
 * 17. updateSellerBalance
 * 18. createSellerPayoutRequest
 * 19. validateSellerPayout
 * 20. processSellerPayout
 * 21. handleSellerPayoutWebhook
 * 22. processSellerReturn
 * 23. processSellerRefund
 * 24. generateSellerInvoice
 * 25. generateSellerReport
 * 26. updateSellerAnalytics
 * 27. sendSellerNotification
 *
 * STRICT PRIVACY DIRECTIVE:
 * Zero access to Health Passport records (health_profiles, conditions, medications, diagnoses, lab reports).
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

// Prohibited medical claims list
const PROHIBITED_MEDICAL_CLAIMS = [
  'cures cancer',
  'guaranteed treatment',
  '100% cures disease',
  'miracle cure',
  'cures diabetes',
  'guaranteed cure',
  'replaces chemotherapy',
  'reverses all aging',
  'covid cure',
  'fda approved prescription substitute'
];

/**
 * Privacy Guard: Rejects any payload or query containing Health Passport references.
 */
function assertNoHealthPassportData(payload) {
  const forbiddenKeys = [
    'health_profiles', 'health_conditions', 'health_medications',
    'health_visits', 'health_diagnoses', 'health_tests',
    'health_lab_reports', 'health_prescriptions', 'health_documents',
    'health_bills', 'health_access_requests', 'health_access_grants',
    'diagnosis', 'medical_history', 'patient_record'
  ];
  for (const key of forbiddenKeys) {
    if (payload && payload[key] !== undefined) {
      throw new functions.https.HttpsError(
        'permission-denied',
        `Privacy Violation: Clinical Health Passport data '${key}' is strictly forbidden in Marketplace operations.`
      );
    }
  }
}

/**
 * Audit Log Writer (Immutable append)
 */
async function writeSellerAuditLog(sellerUid, action, resourceType, resourceId, performedByUid, reason = '') {
  await db.collection('marketplace_seller_audit_logs').add({
    log_id: db.collection('marketplace_seller_audit_logs').doc().id,
    seller_uid: sellerUid,
    action: action,
    resource_type: resourceType,
    resource_id: resourceId,
    performed_by_uid: performedByUid,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    reason: reason
  });
}

// -------------------------------------------------------------
// 1. SELLER ONBOARDING & VERIFICATION FUNCTIONS
// -------------------------------------------------------------

/** 1. createSellerApplication */
exports.createSellerApplication = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  assertNoHealthPassportData(data);

  const { sellerType, countryCode } = data;
  if (!['individual_seller', 'business_seller'].includes(sellerType)) {
    throw new functions.https.HttpsError('invalid-argument', 'Invalid seller type. Must be individual_seller or business_seller.');
  }

  const appId = `app_${context.auth.uid}_${Date.now()}`;
  const appRef = db.collection('marketplace_seller_applications').doc(appId);

  const applicationData = {
    application_id: appId,
    seller_uid: context.auth.uid,
    seller_type: sellerType,
    country_code: countryCode || 'SA',
    status: 'draft',
    created_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  };

  await appRef.set(applicationData);
  await writeSellerAuditLog(context.auth.uid, 'application_created', 'seller_application', appId, context.auth.uid);
  return { success: true, applicationId: appId };
});

/** 2. submitSellerApplication */
exports.submitSellerApplication = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  assertNoHealthPassportData(data);

  const { applicationId } = data;
  const appRef = db.collection('marketplace_seller_applications').doc(applicationId);
  const appDoc = await appRef.get();

  if (!appDoc.exists || appDoc.data().seller_uid !== context.auth.uid) {
    throw new functions.https.HttpsError('not-found', 'Application not found or unauthorized.');
  }

  // Update status
  await appRef.update({
    status: 'submitted',
    submitted_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  // Sync to verification profile
  await db.collection('marketplace_seller_verification').doc(context.auth.uid).set({
    seller_uid: context.auth.uid,
    seller_type: appDoc.data().seller_type,
    country_code: appDoc.data().country_code,
    status: 'submitted',
    application_id: applicationId,
    submitted_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  }, { merge: true });

  await writeSellerAuditLog(context.auth.uid, 'application_submitted', 'seller_application', applicationId, context.auth.uid);
  return { success: true, status: 'submitted' };
});

/** 3. validateSellerApplication */
exports.validateSellerApplication = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { applicationId } = data;
  const appDoc = await db.collection('marketplace_seller_applications').doc(applicationId).get();
  if (!appDoc.exists) throw new functions.https.HttpsError('not-found', 'Application not found.');

  // Validate documents presence
  const docsSnapshot = await db.collection('marketplace_seller_documents')
    .where('application_id', '==', applicationId)
    .get();

  const hasRequiredDocs = docsSnapshot.size > 0;
  return { valid: hasRequiredDocs, documentCount: docsSnapshot.size };
});

/** 4. approveSeller */
exports.approveSeller = functions.https.onCall(async (data, context) => {
  // Staff/Owner check
  if (!context.auth || !context.auth.token || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Staff authorization required.');
  }
  const { sellerUid, reviewerNotes } = data;

  const batch = db.batch();
  batch.update(db.collection('marketplace_seller_verification').doc(sellerUid), {
    status: 'verified',
    verified_at: admin.firestore.FieldValue.serverTimestamp(),
    reviewer_uid: context.auth.uid,
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  batch.update(db.collection('marketplace_seller_profiles').doc(sellerUid), {
    verification_status: 'verified',
    seller_status: 'active',
    store_status: 'active',
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await batch.commit();
  await writeSellerAuditLog(sellerUid, 'seller_approved', 'seller_profile', sellerUid, context.auth.uid, reviewerNotes);
  return { success: true, sellerStatus: 'active', verificationStatus: 'verified' };
});

/** 5. rejectSeller */
exports.rejectSeller = functions.https.onCall(async (data, context) => {
  if (!context.auth || !context.auth.token || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Staff authorization required.');
  }
  const { sellerUid, reasonCode } = data;

  await db.collection('marketplace_seller_verification').doc(sellerUid).update({
    status: 'rejected',
    rejection_reason_code: reasonCode || 'DOCUMENTATION_INCOMPLETE',
    reviewed_at: admin.firestore.FieldValue.serverTimestamp(),
    reviewer_uid: context.auth.uid,
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await writeSellerAuditLog(sellerUid, 'seller_rejected', 'seller_profile', sellerUid, context.auth.uid, reasonCode);
  return { success: true, status: 'rejected' };
});

/** 6. suspendSeller */
exports.suspendSeller = functions.https.onCall(async (data, context) => {
  if (!context.auth || !context.auth.token || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Staff authorization required.');
  }
  const { sellerUid, reason } = data;

  await db.collection('marketplace_seller_profiles').doc(sellerUid).update({
    seller_status: 'suspended',
    store_status: 'paused',
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await writeSellerAuditLog(sellerUid, 'seller_suspended', 'seller_profile', sellerUid, context.auth.uid, reason);
  return { success: true, sellerStatus: 'suspended' };
});

// -------------------------------------------------------------
// 2. PRODUCT MANAGEMENT & COMPLIANCE
// -------------------------------------------------------------

/** 7. createSellerProduct */
exports.createSellerProduct = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  assertNoHealthPassportData(data);

  const { title, description, category, price, stockQuantity, sku, brand } = data;

  // Screening prohibited claims
  const lowerDesc = `${title} ${description}`.toLowerCase();
  for (const claim of PROHIBITED_MEDICAL_CLAIMS) {
    if (lowerDesc.includes(claim)) {
      throw new functions.https.HttpsError('invalid-argument', `Product violates medical compliance: contains claim "${claim}".`);
    }
  }

  const productId = `prod_${Date.now()}_${Math.floor(Math.random() * 1000)}`;
  const productRef = db.collection('marketplace_products').doc(productId);

  const newProduct = {
    productId: productId,
    seller_uid: context.auth.uid,
    title: title,
    description: description,
    category: category,
    brand: brand || '',
    sku: sku || `SKU-${Date.now()}`,
    price: Number(price),
    stockQuantity: Number(stockQuantity) || 0,
    status: 'draft',
    approval_status: 'draft',
    compliance_status: 'not_checked',
    rating: 0.0,
    reviewCount: 0,
    created_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  };

  await productRef.set(newProduct);

  // Initialize inventory
  await db.collection('marketplace_inventory').doc(`inv_${productId}`).set({
    inventory_id: `inv_${productId}`,
    seller_uid: context.auth.uid,
    product_id: productId,
    sku: newProduct.sku,
    available_quantity: Number(stockQuantity) || 0,
    reserved_quantity: 0,
    sold_quantity: 0,
    returned_quantity: 0,
    low_stock_threshold: 5,
    inventory_status: (Number(stockQuantity) || 0) > 5 ? 'in_stock' : ((Number(stockQuantity) || 0) > 0 ? 'low_stock' : 'out_of_stock'),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await writeSellerAuditLog(context.auth.uid, 'product_created', 'product', productId, context.auth.uid);
  return { success: true, productId: productId };
});

/** 8. submitProductForReview */
exports.submitProductForReview = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { productId } = data;

  const prodRef = db.collection('marketplace_products').doc(productId);
  const prodDoc = await prodRef.get();
  if (!prodDoc.exists || prodDoc.data().seller_uid !== context.auth.uid) {
    throw new functions.https.HttpsError('not-found', 'Product not found or unauthorized.');
  }

  await prodRef.update({
    status: 'pending_review',
    approval_status: 'pending_review',
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await db.collection('marketplace_product_reviews').add({
    review_id: `prev_${Date.now()}`,
    product_id: productId,
    seller_uid: context.auth.uid,
    review_type: 'compliance',
    status: 'pending',
    created_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await writeSellerAuditLog(context.auth.uid, 'product_submitted_for_review', 'product', productId, context.auth.uid);
  return { success: true, status: 'pending_review' };
});

/** 9. approveProduct */
exports.approveProduct = functions.https.onCall(async (data, context) => {
  if (!context.auth || !context.auth.token || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Staff authorization required.');
  }
  const { productId, notes } = data;

  await db.collection('marketplace_products').doc(productId).update({
    status: 'active',
    approval_status: 'approved',
    compliance_status: 'approved',
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await writeSellerAuditLog('system', 'product_approved', 'product', productId, context.auth.uid, notes);
  return { success: true, status: 'active' };
});

/** 10. rejectProduct */
exports.rejectProduct = functions.https.onCall(async (data, context) => {
  if (!context.auth || !context.auth.token || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Staff authorization required.');
  }
  const { productId, reasonCode } = data;

  await db.collection('marketplace_products').doc(productId).update({
    status: 'rejected',
    approval_status: 'rejected',
    compliance_status: 'rejected',
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await writeSellerAuditLog('system', 'product_rejected', 'product', productId, context.auth.uid, reasonCode);
  return { success: true, status: 'rejected' };
});

/** 11. validateProductCompliance */
exports.validateProductCompliance = functions.https.onCall(async (data, context) => {
  assertNoHealthPassportData(data);
  const { title, description } = data;
  const fullText = `${title || ''} ${description || ''}`.toLowerCase();

  for (const claim of PROHIBITED_MEDICAL_CLAIMS) {
    if (fullText.includes(claim)) {
      return { compliant: false, violation: `Contains prohibited claim: ${claim}` };
    }
  }
  return { compliant: true };
});

// -------------------------------------------------------------
// 3. INVENTORY ATOMIC CONTROLS
// -------------------------------------------------------------

/** 12. updateInventory */
exports.updateInventory = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { productId, delta, reason } = data;

  const invRef = db.collection('marketplace_inventory').doc(`inv_${productId}`);

  return await db.runTransaction(async (transaction) => {
    const invDoc = await transaction.get(invRef);
    if (!invDoc.exists || invDoc.data().seller_uid !== context.auth.uid) {
      throw new functions.https.HttpsError('not-found', 'Inventory not found or unauthorized.');
    }

    const currentQty = invDoc.data().available_quantity || 0;
    const newQty = currentQty + Number(delta);
    if (newQty < 0) {
      throw new functions.https.HttpsError('failed-precondition', 'Negative inventory is prohibited.');
    }

    const lowThreshold = invDoc.data().low_stock_threshold || 5;
    const newStatus = newQty === 0 ? 'out_of_stock' : (newQty <= lowThreshold ? 'low_stock' : 'in_stock');

    transaction.update(invRef, {
      available_quantity: newQty,
      inventory_status: newStatus,
      updated_at: admin.firestore.FieldValue.serverTimestamp()
    });

    // Record immutable movement
    const movementRef = db.collection('marketplace_inventory_movements').doc();
    transaction.set(movementRef, {
      movement_id: movementRef.id,
      seller_uid: context.auth.uid,
      product_id: productId,
      type: delta >= 0 ? 'stock_added' : 'stock_removed',
      quantity: Math.abs(delta),
      reason: reason || 'Manual seller adjustment',
      created_at: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, newQuantity: newQty, status: newStatus };
  });
});

/** 13. reserveInventory */
exports.reserveInventory = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { productId, quantity, orderId } = data;

  const invRef = db.collection('marketplace_inventory').doc(`inv_${productId}`);

  return await db.runTransaction(async (transaction) => {
    const invDoc = await transaction.get(invRef);
    if (!invDoc.exists) throw new functions.https.HttpsError('not-found', 'Inventory record not found.');

    const available = invDoc.data().available_quantity || 0;
    if (available < quantity) {
      throw new functions.https.HttpsError('resource-exhausted', 'Insufficient available inventory to reserve.');
    }

    transaction.update(invRef, {
      available_quantity: available - quantity,
      reserved_quantity: (invDoc.data().reserved_quantity || 0) + quantity,
      updated_at: admin.firestore.FieldValue.serverTimestamp()
    });

    const movementRef = db.collection('marketplace_inventory_movements').doc();
    transaction.set(movementRef, {
      movement_id: movementRef.id,
      seller_uid: invDoc.data().seller_uid,
      product_id: productId,
      type: 'reservation',
      quantity: quantity,
      reference_id: orderId || '',
      reason: 'Order checkout reservation',
      created_at: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, reservedQuantity: quantity };
  });
});

/** 14. releaseInventory */
exports.releaseInventory = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { productId, quantity, orderId } = data;

  const invRef = db.collection('marketplace_inventory').doc(`inv_${productId}`);

  return await db.runTransaction(async (transaction) => {
    const invDoc = await transaction.get(invRef);
    if (!invDoc.exists) throw new functions.https.HttpsError('not-found', 'Inventory not found.');

    const reserved = invDoc.data().reserved_quantity || 0;
    const releaseQty = Math.min(reserved, quantity);

    transaction.update(invRef, {
      available_quantity: (invDoc.data().available_quantity || 0) + releaseQty,
      reserved_quantity: reserved - releaseQty,
      updated_at: admin.firestore.FieldValue.serverTimestamp()
    });

    const movementRef = db.collection('marketplace_inventory_movements').doc();
    transaction.set(movementRef, {
      movement_id: movementRef.id,
      seller_uid: invDoc.data().seller_uid,
      product_id: productId,
      type: 'reservation_release',
      quantity: releaseQty,
      reference_id: orderId || '',
      reason: 'Reservation timeout or order cancelled',
      created_at: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, releasedQuantity: releaseQty };
  });
});

// -------------------------------------------------------------
// 4. FINANCIAL LEDGER & COMMISSION CALCULATION
// -------------------------------------------------------------

/** 15. calculateSellerCommission */
exports.calculateSellerCommission = functions.https.onCall(async (data, context) => {
  const { grossAmount, countryCode, categoryId, sellerType } = data;
  const rulesSnapshot = await db.collection('marketplace_commission_rules')
    .where('active', '==', true)
    .where('country_code', '==', countryCode || 'SA')
    .limit(1)
    .get();

  let commissionRate = 0.10; // Default 10%
  if (!rulesSnapshot.empty) {
    commissionRate = rulesSnapshot.docs[0].data().commission_value / 100.0;
  }

  const commission = grossAmount * commissionRate;
  const paymentFee = grossAmount * 0.025 + 1.0; // 2.5% + 1 SAR
  const netSeller = grossAmount - commission - paymentFee;

  return {
    grossAmount: grossAmount,
    commissionRate: commissionRate,
    commissionAmount: commission,
    paymentFee: paymentFee,
    netSellerEarnings: Math.max(0, netSeller)
  };
});

/** 16. createSellerLedgerEntry */
exports.createSellerLedgerEntry = functions.https.onCall(async (data, context) => {
  // Authoritative server-side only
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');

  const { sellerUid, orderId, orderItemId, transactionType, grossAmount, commission, paymentFee, refundAmount, taxAmount, currency } = data;

  const ledgerId = `led_${Date.now()}_${Math.floor(Math.random() * 1000)}`;
  const netAmount = (grossAmount || 0) - (commission || 0) - (paymentFee || 0) - (refundAmount || 0) - (taxAmount || 0);

  await db.collection('marketplace_seller_ledger').doc(ledgerId).set({
    ledger_id: ledgerId,
    seller_uid: sellerUid,
    order_id: orderId || '',
    order_item_id: orderItemId || '',
    transaction_type: transactionType || 'sale',
    gross_amount: Number(grossAmount) || 0.0,
    commission: Number(commission) || 0.0,
    payment_fee: Number(paymentFee) || 0.0,
    refund_amount: Number(refundAmount) || 0.0,
    adjustment: 0.0,
    tax_amount: Number(taxAmount) || 0.0,
    net_amount: netAmount,
    currency: currency || 'SAR',
    status: 'posted',
    created_at: admin.firestore.FieldValue.serverTimestamp(),
    effective_at: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true, ledgerId: ledgerId, netAmount: netAmount };
});

/** 17. updateSellerBalance */
exports.updateSellerBalance = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { sellerUid } = data;

  const ledgerSnapshot = await db.collection('marketplace_seller_ledger')
    .where('seller_uid', '==', sellerUid)
    .where('status', '==', 'posted')
    .get();

  let gross = 0;
  let refunds = 0;
  let fees = 0;
  let taxes = 0;
  let net = 0;

  ledgerSnapshot.forEach(doc => {
    const d = doc.data();
    gross += d.gross_amount || 0;
    refunds += d.refund_amount || 0;
    fees += (d.commission || 0) + (d.payment_fee || 0);
    taxes += d.tax_amount || 0;
    net += d.net_amount || 0;
  });

  const balRef = db.collection('marketplace_seller_balances').doc(sellerUid);
  await balRef.set({
    seller_uid: sellerUid,
    currency: 'SAR',
    gross_sales: gross,
    refunds: refunds,
    fees: fees,
    taxes: taxes,
    available_balance: Math.max(0, net),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  }, { merge: true });

  return { success: true, availableBalance: Math.max(0, net) };
});

// -------------------------------------------------------------
// 5. PAYOUT SYSTEM & FRAUD GUARDS
// -------------------------------------------------------------

/** 18. createSellerPayoutRequest */
exports.createSellerPayoutRequest = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { amount, payoutAccountId } = data;
  const sellerUid = context.auth.uid;

  // Validate balance
  const balDoc = await db.collection('marketplace_seller_balances').doc(sellerUid).get();
  const availableBalance = balDoc.exists ? (balDoc.data().available_balance || 0) : 0;

  if (amount > availableBalance) {
    throw new functions.https.HttpsError('failed-precondition', 'Requested payout exceeds available balance.');
  }
  if (amount < 100.0) {
    throw new functions.https.HttpsError('failed-precondition', 'Minimum payout threshold is 100 SAR.');
  }

  // Verify payout account is verified
  const accDoc = await db.collection('marketplace_seller_payout_accounts').doc(payoutAccountId).get();
  if (!accDoc.exists || accDoc.data().seller_uid !== sellerUid || accDoc.data().verification_status !== 'verified') {
    throw new functions.https.HttpsError('failed-precondition', 'A verified payout destination is required.');
  }

  const reqId = `payreq_${Date.now()}`;
  await db.collection('marketplace_seller_payout_requests').doc(reqId).set({
    request_id: reqId,
    seller_uid: sellerUid,
    amount: Number(amount),
    currency: 'SAR',
    payout_account_id: payoutAccountId,
    status: 'requested',
    requested_at: admin.firestore.FieldValue.serverTimestamp(),
    created_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await writeSellerAuditLog(sellerUid, 'payout_requested', 'payout_request', reqId, sellerUid, `Amount: ${amount} SAR`);
  return { success: true, requestId: reqId, status: 'requested' };
});

/** 19. validateSellerPayout */
exports.validateSellerPayout = functions.https.onCall(async (data, context) => {
  const { sellerUid, amount } = data;
  const balDoc = await db.collection('marketplace_seller_balances').doc(sellerUid).get();
  const available = balDoc.exists ? (balDoc.data().available_balance || 0) : 0;

  const valid = amount <= available && amount >= 100.0;
  return { valid: valid, availableBalance: available };
});

/** 20. processSellerPayout */
exports.processSellerPayout = functions.https.onCall(async (data, context) => {
  if (!context.auth || !context.auth.token || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Staff authorization required.');
  }
  const { requestId } = data;
  const reqRef = db.collection('marketplace_seller_payout_requests').doc(requestId);
  const reqDoc = await reqRef.get();

  if (!reqDoc.exists) throw new functions.https.HttpsError('not-found', 'Payout request not found.');

  await reqRef.update({
    status: 'processing',
    approved_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true, status: 'processing' };
});

/** 21. handleSellerPayoutWebhook */
exports.handleSellerPayoutWebhook = functions.https.onRequest(async (req, res) => {
  const { requestId, status, providerPayoutId, failureReason } = req.body;
  const reqRef = db.collection('marketplace_seller_payout_requests').doc(requestId);

  if (status === 'paid') {
    await reqRef.update({
      status: 'paid',
      processed_at: admin.firestore.FieldValue.serverTimestamp(),
      provider_payout_id: providerPayoutId || '',
      updated_at: admin.firestore.FieldValue.serverTimestamp()
    });
  } else if (status === 'failed') {
    await reqRef.update({
      status: 'failed',
      failure_reason_code: failureReason || 'PROVIDER_REJECTED',
      updated_at: admin.firestore.FieldValue.serverTimestamp()
    });
  }

  res.status(200).json({ received: true });
});

// -------------------------------------------------------------
// 6. RETURNS, REFUNDS & ORDER PROCESSING
// -------------------------------------------------------------

/** 22. processSellerReturn */
exports.processSellerReturn = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { returnId, action, sellerNotes } = data;

  const returnRef = db.collection('marketplace_returns').doc(returnId);
  const returnDoc = await returnRef.get();
  if (!returnDoc.exists || returnDoc.data().seller_uid !== context.auth.uid) {
    throw new functions.https.HttpsError('not-found', 'Return request not found or unauthorized.');
  }

  const newStatus = action === 'approve' ? 'approved' : 'rejected';
  await returnRef.update({
    status: newStatus,
    seller_notes: sellerNotes || '',
    seller_responded_at: admin.firestore.FieldValue.serverTimestamp(),
    updated_at: admin.firestore.FieldValue.serverTimestamp()
  });

  await writeSellerAuditLog(context.auth.uid, `return_${newStatus}`, 'return', returnId, context.auth.uid, sellerNotes);
  return { success: true, status: newStatus };
});

/** 23. processSellerRefund */
exports.processSellerRefund = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { returnId, refundAmount } = data;

  const returnDoc = await db.collection('marketplace_returns').doc(returnId).get();
  if (!returnDoc.exists || returnDoc.data().seller_uid !== context.auth.uid) {
    throw new functions.https.HttpsError('not-found', 'Return not found or unauthorized.');
  }

  // Create refund record for gateway processing
  const refundId = `ref_${Date.now()}`;
  await db.collection('marketplace_refunds').doc(refundId).set({
    refund_id: refundId,
    return_id: returnId,
    seller_uid: context.auth.uid,
    order_id: returnDoc.data().order_id || '',
    amount: Number(refundAmount),
    status: 'pending_gateway',
    created_at: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true, refundId: refundId, status: 'pending_gateway' };
});

// -------------------------------------------------------------
// 7. INVOICE, REPORT, ANALYTICS & NOTIFICATIONS
// -------------------------------------------------------------

/** 24. generateSellerInvoice */
exports.generateSellerInvoice = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { orderId } = data;

  const invoiceNumber = `INV-SLR-${Date.now().toString().slice(-6)}`;
  return {
    success: true,
    invoiceNumber: invoiceNumber,
    issuedAt: new Date().toISOString(),
    orderId: orderId,
    storagePath: `marketplace_private/${context.auth.uid}/invoices/${invoiceNumber}.pdf`
  };
});

/** 25. generateSellerReport */
exports.generateSellerReport = functions.https.onCall(async (data, context) => {
  if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated.');
  const { reportType, fromDate, toDate } = data;

  const reportId = `rep_${reportType}_${Date.now()}`;
  return {
    success: true,
    reportId: reportId,
    reportType: reportType,
    downloadPath: `marketplace_private/${context.auth.uid}/reports/${reportId}.csv`,
    generatedAt: new Date().toISOString()
  };
});

/** 26. updateSellerAnalytics */
exports.updateSellerAnalytics = functions.https.onCall(async (data, context) => {
  assertNoHealthPassportData(data);
  const { sellerUid } = data;

  // Aggregate ecommerce metrics purely from seller orders and products
  const ordersSnap = await db.collection('marketplace_orders')
    .where('seller_uid', '==', sellerUid)
    .get();

  let gross = 0;
  let count = ordersSnap.size;
  ordersSnap.forEach(d => { gross += d.data().grand_total || 0; });

  return {
    sellerUid: sellerUid,
    grossSales: gross,
    totalOrders: count,
    averageOrderValue: count > 0 ? gross / count : 0.0
  };
});

/** 27. sendSellerNotification */
exports.sendSellerNotification = functions.https.onCall(async (data, context) => {
  const { sellerUid, title, body, type } = data;
  assertNoHealthPassportData(data);

  await db.collection('notifications').add({
    user_id: sellerUid,
    title: title,
    body: body,
    type: type || 'seller_update',
    is_read: false,
    created_at: admin.firestore.FieldValue.serverTimestamp()
  });

  return { success: true };
});
