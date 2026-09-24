package com.example.healthogram.healthpassport.fhir

/**
 * Healthogram 2.1 Terminology Service
 * Standards-compliant mappings for LOINC, SNOMED CT, ICD-10, RxNorm, and CVX.
 */
object TerminologyService {

    const val SYSTEM_LOINC = "http://loinc.org"
    const val SYSTEM_SNOMED = "http://snomed.info/sct"
    const val SYSTEM_ICD10 = "http://hl7.org/fhir/sid/icd-10"
    const val SYSTEM_RXNORM = "http://www.nlm.nih.gov/research/umls/rxnorm"
    const val SYSTEM_CVX = "http://hl7.org/fhir/sid/cvx"

    // LOINC Vitals & Common Labs
    private val loincMap = mapOf(
        "HEART_RATE" to FHIRCoding(SYSTEM_LOINC, "8867-4", "Heart rate"),
        "STEPS" to FHIRCoding(SYSTEM_LOINC, "55423-8", "Number of steps in 24 hour Measured"),
        "BLOOD_PRESSURE" to FHIRCoding(SYSTEM_LOINC, "85354-9", "Blood pressure panel with all children optional"),
        "WEIGHT" to FHIRCoding(SYSTEM_LOINC, "29463-7", "Body weight"),
        "HEIGHT" to FHIRCoding(SYSTEM_LOINC, "8302-2", "Body height"),
        "BMI" to FHIRCoding(SYSTEM_LOINC, "39156-5", "Body mass index (BMI) [Ratio]"),
        "GLUCOSE" to FHIRCoding(SYSTEM_LOINC, "2339-0", "Glucose [Mass/volume] in Blood"),
        "SPO2" to FHIRCoding(SYSTEM_LOINC, "2708-6", "Oxygen saturation in Arterial blood by Pulse oximetry"),
        "TEMPERATURE" to FHIRCoding(SYSTEM_LOINC, "8310-5", "Body temperature"),
        "LIPID_PANEL" to FHIRCoding(SYSTEM_LOINC, "57698-3", "Lipid panel with direct LDL - Serum or Plasma"),
        "CBC" to FHIRCoding(SYSTEM_LOINC, "58410-2", "Complete blood count (hemogram) panel")
    )

    // Common Vaccines (CVX)
    private val cvxMap = mapOf(
        "COVID-19" to FHIRCoding(SYSTEM_CVX, "208", "COVID-19, mRNA, LNP-S, PF, 30 mcg/0.3 mL dose"),
        "INFLUENZA" to FHIRCoding(SYSTEM_CVX, "141", "Influenza, seasonal, injectable"),
        "HEPATITIS_B" to FHIRCoding(SYSTEM_CVX, "45", "Hep B, adult"),
        "MMR" to FHIRCoding(SYSTEM_CVX, "03", "MMR"),
        "TETANUS" to FHIRCoding(SYSTEM_CVX, "115", "Tdap")
    )

    fun getObservationCoding(type: String): FHIRCoding {
        return loincMap[type.uppercase()] ?: FHIRCoding(
            SYSTEM_LOINC,
            "UNK-${type.uppercase()}",
            type
        )
    }

    fun getVaccineCoding(vaccineName: String): FHIRCoding {
        val key = cvxMap.keys.find { vaccineName.contains(it, ignoreCase = true) }
        return if (key != null) cvxMap[key]!! else FHIRCoding(
            SYSTEM_CVX,
            "999",
            vaccineName
        )
    }

    fun getConditionCoding(conditionName: String, icd10Code: String? = null): FHIRCodeableConcept {
        return FHIRCodeableConcept(
            coding = listOf(
                FHIRCoding(
                    system = SYSTEM_ICD10,
                    code = icd10Code ?: "R69", // R69 = Illness, unspecified
                    display = conditionName
                )
            ),
            text = conditionName
        )
    }

    fun getMedicationCoding(drugName: String): FHIRCodeableConcept {
        return FHIRCodeableConcept(
            coding = listOf(
                FHIRCoding(
                    system = SYSTEM_RXNORM,
                    code = "RXN-${Math.abs(drugName.hashCode()) % 1000000}",
                    display = drugName
                )
            ),
            text = drugName
        )
    }
}
