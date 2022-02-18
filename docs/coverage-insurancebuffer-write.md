# Coverage Write (Insurance Buffer)

## Supported fields

|Path|Required|Notes|
|---|---|---|
|`.resourceType` | Required | Must be `Coverage`. |
| `.id` | Conditional | Should be omitted on create. |
| `.contained[]` | Required | See [Contained Resources](#contained-resources) below for a list of contained resources that must exist in any order. |
| `.status` | Required | Must be `draft`. |
| `.type` | Required | |
| `.type.coding[]` | Required | |
| `.type.coding[0].system` | Required | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.8808001`.|
| `.type.coding[0].code` | Required | ToDo |
| `.subscriberId` | Required | |
| `.beneficiary.reference` | Required | Must be full or relative URL to `Patient` resource. |
| `.beneficiary.identifier` | Required | |
| `.beneficiary.identifier.type.coding[0].code` | Required | Must be `MB`. |
| `.beneficiary.identifier.value` | Required | Subscriber's primary ID number. This number is assigned by the payer and can be found on the subscriber's insurance card. |
| `.relationship.coding[]` | Required | Must contain 1 entry. |
| `.relationship.coding[0].system` | Required | Must be `http://terminology.hl7.org/CodeSystem/subscriber-relationship` |
| `.relationship.coding[0].code` | Required | X.12 271 EB03 code. 1-2 characters in length. |
| `.payor[]` | Required | Must contain 1 entry. |
| `.payor[0].reference` | Required | Must be a reference to an `Organization` resource in the `.contained[]` field. |
| `.class[]` | Required | Must contain 1 entry. |
| `.class[0].type.coding[0].system` | Required | Must be `http://terminology.hl7.org/CodeSystem/coverage-class`. |
| `.class[0].type.coding[0].code` | Required | Must be `group`. |
| `.class[0].value` | Required | Must be a reference to an `InsurancePlan` resource in the `.contained[]` field. |

## Contained Resources

### InsurancePlan

#### Supported Fields

|Path|Required|Notes|
|---|---|---|
|`.resourceType` | Required | Must be `InsurancePlan`. |
| `.id` | Required | Must match the id provided in the `Coverage` `.class[0].value` field. |
| `.extension[]` | Required | See [Supported Extensions](#supported-extensions) below. |
| `.identifier[]` | Required | See [Supported Identifiers](#supported-identifiers) below. |
| `.name` | Optional | This is the name that the insurance company uses to identify the plan. |
| `.plan[]` | Required | Must contain 1 entry. |
| `.plan[0].type.coding[0].system` | Required | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.408009`. |
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
| `http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare` | `valueBoolean` | Required | Answer `true` if this plan requires certification of ambulatory procedures. This may include Ambulatory surgeries, CAT scans, MRI, non-invasive procedures, etc. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions` | `valueBoolean` | Required | Answer `true` if the policy excludes any pre existing conditions. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable` | `valueBoolean` | Required | If this policy will allow assignment of benefits then answer `true`. |
#### Supported Identifiers

Several identifiers are supported.
<details>
<summary><strong>Group Number</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.system` | Required | `urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.908002` |
| `.value` | Required | If this is a group policy enter the number which identifies this policy, i.e. group number/code that the insurance company uses to identify this  plan. Must be unique. |

</details>

<details>
<summary><strong>Banking Identification Number</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.system` | Optional | `urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.40801` |
| `.value` | Optional | The Plan's Banking Identification Number (BIN). Used for NCPDP  transmissions. |
</details>

<details>
<summary><strong>Processor Control Number</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.system` | Optional | `urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.408011` |
| `.value` | Optional | The Plan's Processor Control Number (PCN). Used for NCPDP  transmissions. |
</details>

### Organization

#### Supported Fields

|Path|Required|Notes|
|---|---|---|
|`.resourceType` | Required | Must be `Organization`. |
| `.id` | Required | Must match the id provided in the `Coverage` `.payor[0]` field. |
| `.name` | Required | |



