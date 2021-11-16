# InsurancePlan Write

## Supported fields

|Path|Required| Notes|
|---|---|---|
|`.resourceType` | Required | Must be `InsurancePlan`. |
|`.id` | Conditional | Should be omitted on create. Must match InsurancePlan ID in URL on update. |
| `.extension[]` | Required | See [Supported Extensions](#supported-extensions) below. |
| `.identifier[]` | Required | See [Supported Identifiers](#supported-identifiers) below. |
| `.type[]` | Required | See [Supported Types](#supported-types) below. |
| `.name` | Required | This is the name that the insurance company uses to identify the plan. |
| `.ownedBy.reference` | Required | The insurance company that this policy is with. Must be a relative URL to an `Organization`, e.g `Organization/I3-450NAk1LKUAaaGqyCDA9S9` |
| `.plan[]` | Required | Must contain 1 entry. |
| `.plan[0].type.coding[0].system` | Required | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009`. |
| `.plan[0].type.coding[0].display` | Required | Name of the plan type. See _Type of Plan_ defined below. |

<details>
<summary><strong>Type of Plan</strong></summary>

System `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009`

The type of plan may be dependent on the type of coverage provided by the insurance company and may affect the type of benefits that are available for the plan.

- `ACCIDENT AND HEALTH INSURANCE`
- `AUTOMOBILE`
- `AVIATION TRIP INSURANCE`
- `BLUE CROSS/BLUE SHIELD`
- `CARVE-OUT`
- `CATASTROPHIC INSURANCE`
- `CHAMPVA`
- `COINSURANCE`
- `COMPREHENSIVE MAJOR MEDICAL`
- `DENTAL INSURANCE`
- `DUAL COVERAGE`
- `EXCLUSIVE PROVIDER ORGANIZATION`
- `HEALTH MAINTENANCE ORGANIZ`
- `HEALTH MAINTENANCE ORGANIZATION W/OUT OF NETWORK BENEFITS`
- `HIGH DEDUCTIBLE HEALTH PLAN`
- `HIGH DEDUCTIBLE HEALTH PLAN W/HEALTH SAVINGS ACCOUNT`
- `HIGH DEDUCTIBLE HEALTH PLAN W/HEALTH REIMBURSEMENT ARRANGEMENT`
- `HOSPITAL-MEDICAL INSURANCE`
- `INCOME PROTECTION (INDEMNITY)`
- `INDIVIDUAL PRACTICE ASSOCATION (IPA)`
- `INPATIENT (BASIC HOSPITAL)`
- `KEY-MAN HEALTH INSURANCE`
- `LABS, PROCEDURES, X-RAY, ETC. (ONLY)`
- `MAJOR MEDICAL EXPENSE INSURANCE`
- `MANAGED CARE SYSTEM (MCS)`
- `MEDI-CAL`
- `MEDICAID`
- `MEDICAL EXPENSE (OPT/PROF)`
- `MEDICARE (M)`
- `MEDICARE ADVANTAGE`
- `MEDICARE/MEDICAID (MEDI-CAL)`
- `MEDICARE SECONDARY (B EXC)`
- `MEDICARE SECONDARY (NO B EXC)`
- `MEDICARE SUPPLEMENTAL`
- `MEDIGAP PLAN C`
- `MEDIGAP PLAN F`
- `MEDIGAP PLAN A`
- `MEDIGAP PLAN B`
- `MEDIGAP PLAN D`
- `MEDIGAP PLAN G`
- `MEDIGAP PLAN K`
- `MEDIGAP PLAN L`
- `MEDIGAP PLAN M`
- `MEDIGAP PLAN N`
- `MENTAL HEALTH`
- `NO-FAULT INSURANCE`
- `POINT OF SERVICE`
- `PREFERRED PROVIDER ORGANIZATION (PPO)`
- `PREPAID GROUP PRACTICE PLAN`
- `PRESCRIPTION`
- `QUALIFIED IMPAIRMENT INSURANCE`
- `REGULAR MEDICAL EXPENSE INSURANCE`
- `RETIREE`
- `SPECIAL CLASS INSURANCE`
- `SPECIAL RISK INSURANCE`
- `SPECIFIED DISEASE INSURANCE`
- `SURGICAL EXPENSE INSURANCE`
- `TORT FEASOR`
- `TRICARE`
- `TRICARE SUPPLEMENTAL`
- `VA SPECIAL CLASS`
- `VISION`
- `WORKERS' COMPENSATION INSURANCE`

</details>

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired` | `valueBoolean` | Required | Answer `true` if Utilization Review is required by the insurance company for this policy. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired` | `valueBoolean` | Required | Answer `true` if this policy requires Pre-certification of all non-emergent admissions. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions` | `valueBoolean` | Required | Answer `true` if the policy excludes any pre existing conditions. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable` | `valueBoolean` | Required | If this policy will allow assignment of benefits then answer `true`. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare` | `valueBoolean` | Required | Answer `true` if this plan requires certification of ambulatory procedures. This may include Ambulatory surgeries, CAT scans, MRI, non-invasive procedures, etc. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame` | `valueQuantity` | Required | (Required) `.unit` This is the standard filing time frame for the insurance plan. See _Insurance Filing Time Frame Codes_ defined below. <br> (Required) `.value` Enter the value corresponding to the Standard Filing Time Frame.  For example, for the time frame of DAYS, enter the number of days.|

<details>
<summary><strong>Insurance Filing Time Frame Codes</strong></summary>

System `urn:oid:2.16.840.1.113883.3.8901.3.3558013`

Used with

- `http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame`

The time frame is the maximum amount of time from the date of service that the insurance company or plan allows for submitting claims.

- `DAYS`
- `DAYS OF FOLLOWING YEAR`
- `DAYS PLUS ONE YEAR`
- `END OF FOLLOWING YEAR`
- `MONTH(S)`
- `MONTHS OF FOLLOWING YEAR`
- `NO FILING TIME FRAME LIMIT`
- `YEAR(S)`

</details>

## Supported Identifiers
Several identifiers are supported.
<details>
<summary><strong>Group Number</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.system` | Required | `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002` |
| `.value` | Required | If this is a group policy enter the number which identifies this policy,  i.e. group number/code that the insurance company uses to identify this  plan. Must be unique. |
</details>

<details>
<summary><strong>Plan ID</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.system` | Required | `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001` |
| `.value` | Required | The Group Insurance Plan's Plan ID. |
</details>

<details>
<summary><strong>Banking Identification Number</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.system` | Optional | `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68002` |
| `.value` | Optional | The Plan's Banking Identification Number (BIN). Used for NCPDP  transmissions. |
</details>

<details>
<summary><strong>Processor Control Number</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.system` | Optional | `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68003` |
| `.value` | Optional | The Plan's Processor Control Number (PCN). Used for NCPDP  transmissions. |
</details>

## Supported Types
Several plans are supported.

<details>
<summary><strong>Plan Category</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.coding[0].system` | Conditional | `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8014` |
| `.coding[0].code` | Conditional | If the Type of Plan's Major Category is MEDICARE, this field should contain the specific type of coverage that this plan represents. See example [InsurancePlanWithMedicare](../vista-fhir-query/samples/insurancePlanMedicareCreate.json) |

- `A` for MEDICARE PART A
- `B` for MEDICARE PART B
- `C` for MEDICARE OTHER

</details>

<details>
<summary><strong>Electronic Plan Type</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.coding[0].system` | Required | `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8015` |
| `.coding[0].code` | Required | This field contains the X12 data to identify the source of pay type. |

- `12` for PPO
- `13` for POS
- `15` for INDEMNITY
- `16` for HMO MEDICARE
- `17` for DENTAL
- `BL` for BC/BS
- `CH` for TRICARE
- `CI` for COMMERCIAL
- `DS` for DISABILITY
- `FI` for FEP - Do not use for BC/BS
- `HM` for HMO
- `MC` for MEDICAID
- `MX` for MEDICARE A or B
- `TV` for TITLE V
- `ZZ` for OTHER
</details>

> **Unsupported fields or extensions will be ignored; all supported fields and extensions are listed below.**

Example: [InsurancePlan](../vista-fhir-query/samples/insurancePlanCreate.json)
