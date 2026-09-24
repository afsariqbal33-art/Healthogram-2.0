/**
 * Healthogram 2.0 Search Indexing & Query API
 * Implements ADR-015: Pluggable Search Provider Abstraction.
 */
const { db, ErrorCodes } = require('../shared');

/**
 * Indexes a public entity document into the search collection.
 * Rejects any document attempting to index clinical health data.
 */
async function indexSearchDocument(data, context) {
  if (!context.auth) throw new Error(ErrorCodes.AUTH_REQUIRED);

  const { documentId, entityType, title, subtitle, tags, country, city, isVerified, priceInCents } = data;

  if (!documentId || !entityType || !title) {
    throw new Error(ErrorCodes.VALIDATION_FAILED + ': Missing documentId, entityType, or title');
  }

  // Security Check: Absolute ban on clinical records
  const textBlob = `${title} ${subtitle || ''} ${(tags || []).join(' ')}`.toLowerCase();
  const forbidden = ['health_passport', 'diagnosis', 'prescription', 'lab_result', 'allergy'];
  if (forbidden.some(kw => textBlob.includes(kw))) {
    throw new Error(ErrorCodes.PERMISSION_DENIED + ': Clinical records cannot be indexed in search');
  }

  await db.collection('search_index').doc(documentId).set({
    document_id: documentId,
    entity_type: entityType,
    title,
    subtitle: subtitle || '',
    tags: tags || [],
    country: (country || 'OM').toUpperCase(),
    city: city || '',
    is_verified: !!isVerified,
    price_in_cents: priceInCents !== undefined ? priceInCents : null,
    updated_at: new Date()
  });

  return { success: true, documentId };
}

/**
 * Queries the decoupled search index.
 */
async function searchEntities(data, context) {
  const { query, entityTypes, country, onlyVerified, limit = 20 } = data;

  let firestoreQuery = db.collection('search_index');

  if (country) {
    firestoreQuery = firestoreQuery.where('country', '==', country.toUpperCase());
  }

  if (onlyVerified) {
    firestoreQuery = firestoreQuery.where('is_verified', '==', true);
  }

  const snap = await firestoreQuery.limit(limit).get();
  const results = [];

  const lowerQuery = (query || '').toLowerCase().trim();

  snap.forEach(doc => {
    const item = doc.data();
    if (entityTypes && entityTypes.length > 0 && !entityTypes.includes(item.entity_type)) {
      return;
    }
    if (lowerQuery) {
      const matchBlob = `${item.title} ${item.subtitle} ${(item.tags || []).join(' ')}`.toLowerCase();
      if (!matchBlob.includes(lowerQuery)) {
        return;
      }
    }
    results.push(item);
  });

  return { results, totalHits: results.length };
}

module.exports = {
  indexSearchDocument,
  searchEntities
};
