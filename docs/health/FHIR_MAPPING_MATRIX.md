# HL7 FHIR R4 Mapping Matrix & Coverage Specification

## 1. Resource Mapping Matrix

The following matrix documents bidirectional mappings between Healthogram internal domain entities and HL7 FHIR R4 standard resources:

| Healthogram Entity | FHIR Resource | Cardinality | Standard Terminologies | Mapped Fields | Unmapped / Internal Only Fields |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `HealthProfile` / `User` | `Patient` | 1:1 | OID, ISO 5218 (Gender) | `healthId`, `name`, `gender`, `dateOfBirth`, `active` | Private profile settings, social followings, avatar URLs |
| `DoctorProfile` | `Practitioner` | 1:1 | National Medical Licensing | `uid`, `name`, `specialty`, `licenseNumber` | Consultation fees, creator tips, wallet balances |
| `Clinic` / `Hospital` | `Organization` | 1:1 | Commercial Registration | `organizationId`, `name`, `type` (clinic, hospital) | Operating hours, marketplace ratings, revenue ledgers |
| `HealthCondition` | `Condition` | 1:1 | ICD-10, SNOMED CT | `conditionCode`, `conditionName`, `status`, `diagnosedDate`, `patientUid` | Internal severity slider, private clinician notes |
| `HealthAllergy` | `AllergyIntolerance` | 1:1 | SNOMED CT, RxNorm | `allergen`, `severity`, `status`, `reaction`, `patientUid` | User verification badges |
| `HealthMedication` | `MedicationRequest` | 1:1 | RxNorm, NDC | `medicineName`, `dosage`, `frequency`, `prescribedBy`, `startDate` | Local pharmacy dispensing hints |
| `HealthMedication` (Catalog) | `Medication` | 1:1 | RxNorm | `medicineCode`, `medicineName`, `status` | Local manufacturer IDs |
| `HealthObservation` | `Observation` | 1:1 | LOINC, UCUM | `observationType`, `valueNumeric`, `unit`, `effectiveTimestamp`, `patientUid` | Freeform internal user annotations |
| `StructuredLabResultItem` | `DiagnosticReport` | 1:M | LOINC | `testName`, `testCode`, `status`, `completedTimestamp`, `conclusion` | Sample barcode raw tracking identifiers |
| `HealthProcedure` | `Procedure` | 1:1 | CPT, SNOMED CT | `procedureName`, `procedureCode`, `status`, `performedDate` | Billing item line references |
| `HealthImmunization` | `Immunization` | 1:1 | CVX | `vaccineName`, `vaccineCode`, `doseNumber`, `administeredDate`, `lotNumber` | Clinic batch storage room number |
| `HealthDocument` | `DocumentReference` | 1:1 | LOINC, MIME types | `docId`, `documentType`, `mimeType`, `storageUri`, `dateCreated` | Cloud Storage bucket internal cluster IDs |
| `HealthCarePlan` | `CarePlan` | 1:1 | SNOMED CT | `title`, `description`, `status`, `periodStart`, `periodEnd` | Gamified streak points |
| `AppointmentBooking` | `Appointment` | 1:1 | HL7 AppointmentStatus | `appointmentId`, `scheduledStart`, `scheduledEnd`, `status`, `location` | Payment gateway transaction trace IDs |
| `HealthReferral` | `ServiceRequest` | 1:1 | SNOMED CT | `referralReason`, `specialty`, `priority`, `referringDoctorUid` | Referral commission metadata |
| `HealthEncounterSummary` | `Encounter` | 1:1 | HL7 EncounterType | `encounterId`, `encounterType`, `startTimestamp`, `endTimestamp`, `chiefComplaint` | Facility room bed number |

---

## 2. Inbound / Outbound Transformation Guarantees
1. **Never Discard Data Silently**: If an inbound FHIR resource contains extensions or fields not represented in Healthogram's primary model, they are captured in `HealthRecordProvenance.modificationNotes` and preserved in the raw JSON payload.
2. **Deterministic Export**: Exporting a record to FHIR Bundle and re-importing yields exact clinical equivalence without data corruption.
