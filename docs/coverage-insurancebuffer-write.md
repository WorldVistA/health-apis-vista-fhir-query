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
| `.effectiveDate` | Required | The date this policy went into effect for this patient. Must not be after expiration date |
| `.expirationDate` | Required | The date this policy expires for this patient. Must not be before effective date. |
| `.relationship.coding[]` | Required | Must contain 1 entry. |
| `.relationship.coding[0].system` | Required | Must be `http://terminology.hl7.org/CodeSystem/subscriber-relationship` |
| `.relationship.coding[0].code` | Required | Must be one of the codes defined in the above system.  |
| `.payor[]` | Required | Must contain 1 entry. |
| `.payor[0].reference` | Required | Must be a reference to an `Organization` resource in the `.contained[]` field. |
| `.subscriber` | Conditional | Required unless the `relationship.coding[0].code` is `self`. |
| `.subscriber.reference` | Optional | Should  be a reference to a `RelatedPerson` resource in the `.contained[]` field. |
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
| `.name` | Required | The name of the Insurance Carrier that provides coverage for this patient. 3-30 characters in length. |
|`.address[]` | Optional | Should contain at most 1 entry. |
|`.address[0].line[]` | Optional | Should contain 1 to 3 items. |
|`.address[0].line[0]` | Optional | 3-35 characters. |
|`.address[0].line[1]` | Optional | 3-30 characters. |
|`.address[0].line[2]` | Optional | 3-30 characters. |
|`.address[0].city` | Optional | 2-25 characters. |
|`.address[0].state` | Optional | 2 characters. |
|`.address[0].postalCode` | Optional | Format `[0-9]{9}` or `[0-9]{5}-[0-9]{4}`. |
| `contact.telecom.value` | Optional | The insurance carriers phone number where specific inquires should be made. 7-20 characters in length.
| `contact.telecom.system` | Optional | Should be `phone`.
| `contact.purpose` | Optional | Should be `BILL` or `PRECERT`.
| `.telecom.system` | Optional | Should be `phone`.
| `.telecom.value` | Optional | The phone number at which this insurance company can be reached. 7-20 characters in length.
| `.extension[]` | Optional | See [Supported Extensions](#supported-extensions) below. |

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare` | `valueCodeableConcept` | Optional | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.208005`. `.coding[0].value` is _
Will Reimburse For Care Codes_ defined below. |



### Will Reimburse For Care Codes

System `urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.208005`

Used with

- `http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare`

This code denotes under which circumstances this insurance carrier will reimburse the Dept. of Veterans Affairs for care
received.

- `REIMBURSE`
- `WILL REIMBURSE IF TREATED UNDER VAR 6046(C) OR VAR 6060.2(A)`
- `DEPENDS ON POLICY, CHECK WITH COMPANY`
- `WILL NOT REIMBURSE`

### RelatedPerson

#### Supported Fields

|Path|Required|Notes|
|---|---|---|
|`.resourceType` | Required | Must be `RelatedPerson`. |
| `.id` | Required | Must match the id provided in the `Coverage` `.subscriber` field. |
| `.birthDate` | Conditional | The Date of Birth of the policy holder. Must be populated if the patient is not the subscriber. |
| `.identifier[]` | Optional | Should contain an entry if patient is not the insured. |
| `.identifier[0].system` | Optional | Should be `http://hl7.org/fhir/sid/us-ssn` |
| `.identifier[0].value` | Optional | Insured's SSN. Must be 9-13 characters in length. |
|`.address[]` | Optional | Should contain at most 1 entry. |
|`.address[0].line[]` | Optional | Should contain 0 to 2 items. |
|`.address[0].line[0]` | Optional | Subscriber's street line 1, 1-55 characters. |
|`.address[0].line[1]` | Optional | Subscriber's street line 2, 1-55 characters. |
|`.address[0].city` | Optional | Subscriber's city, 1-30 characters. |
|`.address[0].state` | Optional | Subscriber's state, 2 characters. |
|`.address[0].country` | Optional | Subscriber's country, 2-3 characters. |
|`.address[0].district` | Optional | Subscriber's district, 1-3 characters. |
|`.address[0].postalCode` | Optional | Subscriber's zipcode,1-15 characters. |
| `.telecom.system` | Optional | Should be `phone`.
| `.telecom.value` | Optional | The phone number at which this insurance company can be reached. 7-20 characters in length.
| `.extension[]` | Optional | See [Supported Extensions](#supported-extensions) below. |

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/core/STU4/StructureDefinition-us-core-birthsex` | `valueCode` | Required |  `.valueCode` is _
Administrative Gender Codes_ defined below. |

### Administrative Gender Codes
Used with

- `http://hl7.org/fhir/us/core/STU4/StructureDefinition-us-core-birthsex`

This code denotes under which circumstances this insurance carrier will reimburse the Dept. of Veterans Affairs for care
received.

| Code | Display
|---|---|
|`F` | `Female`
|`M` | `Male`
|`UNK` | `Unknown`
