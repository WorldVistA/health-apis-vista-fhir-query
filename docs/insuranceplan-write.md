# InsurancePlan Write

## Supported fields

|Path|Required|Notes|
|---|---|---|
|`.resourceType` | Required | Must be `InsurancePlan`. |
|`.id` | Conditional | Should be omitted on create. Must match InsurancePlan ID in URL on update. |
| `.extension[]` | Required | See [Supported Extensions](#supported-extensions) below. |
| `.identifier[]` | Required | See [Supported Identifiers](#supported-identifiers) below. |
| `.type[]` | Required | See [Supported Types](#supported-types) below. |
| `.name` | Required | This is the name that the insurance company uses to identify the plan. |
| `.ownedBy.reference` | Required | The insurance company that this policy is with. Must be a relative URL to an `Organization`.  |
| `.plan[]` | Required | Must contain 1 entry. |
| `.plan[0].type.coding[0].system` | Required | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009`. |
| `.plan[0].type.coding[0].code` | Optional | Code corresponding to plan type name, will be auto-populated if unset or incorrect. |
| `.plan[0].type.coding[0].display` | Required | Name of the plan type. |
| `.plan[0].text` | Optional | Same as value as `.plan[0].type.coding[0].display`, will be auto-populated if unset or incorrect. |

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired` | `valueBoolean` | Required | Answer `true` if Utilization Review is required by the insurance company for this policy. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired` | `valueBoolean` | Required | Answer `true` if this policy requires Pre-certification of all non-emergent admissions. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions` | `valueBoolean` | Required | Answer `true` if the policy excludes any pre existing conditions. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable` | `valueBoolean` | Required | If this policy will allow assignment of benefits then answer `true`. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare` | `valueBoolean` | Required | Answer `true` if this plan requires certification of ambulatory procedures. This may include Ambulatory surgeries, CAT scans, MRI, non-invasive procedures, etc. |
| `http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame` | `valueQuantity` | Required | (Required) `.unit` This is the standard filing time frame for the insurance plan. Must be one of `DAYS`, `MONTH(S)`, `YEAR(S)`, `DAYS PLUS ONE YEAR`, `DAYS OF FOLLOWING YEAR`, `MONTHS OF FOLLOWING YEAR`, `END OF FOLLOWING YEAR`, `NO FILING TIME FRAME LIMIT` <br> (Required) `.value` Enter the value corresponding to the Standard Filing Time Frame.  For example, for the time frame of DAYS, enter the number of days.|
## Supported Identifiers

| System | Required | Notes |
|---|---|---|
| `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002` | Required | If this is a group policy enter the number which identifies this policy,  i.e. group number/code that the insurance company uses to identify this  plan. Must be unique. |
| `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001` | Required | The Group Insurance Plan's Plan ID. |
| `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68002` | Optional | The Plan's Banking Identification Number (BIN). Used for NCPDP  transmissions. |
| `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68003` | Optional | The Plan's Processor Control Number (PCN). Used for NCPDP transmissions. |

## Supported Types

| System | Required | Notes |
|---|---|---|
| `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8014` | Conditional | If the Type of Plan's Major Category is MEDICARE, this field should contain the specific type of coverage that this plan represents. See example [InsurancePlanWithMedicare](../vista-fhir-query/samples/insurancePlanMedicareCreate.json) |
| `urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8015` | Required | This field contains the X12 data to identify the source of pay type. |

> **Unsupported fields or extensions will be ignored; all supported fields and extensions are listed below.**

Example: [InsurancePlan](../vista-fhir-query/samples/insurancePlanCreate.json)
