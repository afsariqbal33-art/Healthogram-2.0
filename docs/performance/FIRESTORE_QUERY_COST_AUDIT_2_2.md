# HEALTHOGRAM 2.2 FIRESTORE QUERY COST AUDIT

**Cost Basis:** Google Cloud Firestore Pricing (Native Mode, Multi-Region `eur3`)  
- Document Reads: $0.06 per 100,000 reads  
- Document Writes: $0.18 per 100,000 writes  
- Document Deletes: $0.02 per 100,000 deletes  
- Storage: $0.18 per GB/month  

---

## 1. High-Traffic Query Cost Model (Baseline 100,000 Daily Active Users)

| Subsystem Query | Daily Read Volume | Daily Write Volume | Monthly Read Cost | Monthly Write Cost | Total Monthly Cost | Optimization Applied |
| :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **Social Feed Cursor (25 docs)** | 2,500,000 | 80,000 | $45.00 | $4.32 | $49.32 | 2-minute memory cache, cursor limit(25) |
| **Reels Feed Cursor (20 docs)** | 1,800,000 | 40,000 | $32.40 | $2.16 | $34.56 | Prefetch first 3 reels only |
| **Marketplace Catalog Search** | 850,000 | 25,000 | $15.30 | $1.35 | $16.65 | Country/category compound index limits |
| **Health Passport Timeline** | 300,000 | 15,000 | $5.40 | $0.81 | $6.21 | Summary-only load; detailed PHI on click |
| **User Profile & Follow State** | 900,000 | 30,000 | $16.20 | $1.62 | $17.82 | Local cache TTL 5 minutes |
| **Notifications Feed** | 600,000 | 120,000 | $10.80 | $6.48 | $17.28 | Badge counter cached in user profile |
| **Chats & Message History** | 1,200,000 | 450,000 | $21.60 | $24.30 | $45.90 | Ephemeral typing routed to Realtime DB |
| **Order History & Tracking** | 180,000 | 45,000 | $3.24 | $2.43 | $5.67 | Indexed suborders by buyer/seller UID |
| **Owner Earnings Aggregation** | 50,000 | 12,000 | $0.90 | $0.65 | $1.55 | Daily rollup documents; zero raw scans |
| **TOTALS (Estimated 100k DAU)** | **8,380,000** | **817,000** | **$150.84** | **$44.12** | **$194.96** | **Pristine efficiency under $200/mo** |

---

## 2. Waste Avoidance Invariants

1. **No Unbounded Queries:** Every single `.get()` or `.collection().where()` call across the repository enforces an explicit `.limit()`.
2. **Zero Client-Side Collection Filtering:** Queries filter via indexed Firestore clauses; clients never pull 5,000 records to filter 10 in memory.
3. **No Redundant Counter Updates:** Social likes do not rewrite the author's primary profile document. Counter shards absorb write bursts.
4. **Listener Pruning:** Realtime snapshot listeners are destroyed when the enclosing composable leaves composition or ViewModel is cleared.
