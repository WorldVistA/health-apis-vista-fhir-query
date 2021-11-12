# CoverageEligibilityResponse Write

## Supported Fields

|Path|Required|Notes|
|---|---|---|
| `.resourceType` | Required | Must be `CoverageEligibilityResponse`. |
| `.id` | Conditional | Should be omitted on create. |
| `.status` | Required | Must be `active`. |
| `.purpose[]` | Required | Must contain 1 entry. |
| `.purpose[0]` | Required | Must be `benefits`. |
| `.patient.reference` | Required | Must be a full or relative url to the `Patient` resource. |
| `.servicedDate` | Required | |
| `.created` | Required | |
| `._request` | Required | Must be a data absent reason extension. |
| `.outcome` | Required | Must be `complete`. |
| `.insurer.reference` | Required | Must be a full or relative url to an `Organization` with type `pay`. |
| `.extension[]` | Required | See [Supported Extensions](#supported-extensions) below. |
| `.identifier[]` | Required | See [Supported Identifiers](#supported-identifiers) below. |
| `.insurance[]` | Required | ??? ToDo -- come back and reevaluate this statement after KBS feedback -- Must contain 1 entry. |
| `.insurance[0].extension[]` | Required | See [Insurance Extensions](#insurance-extensions) below. |
| `.insurance[0].coverage.reference` | Required | Must be a full or relative url to the `Coverage` resource. |
| `.insurance[0].inforce` | Required | - `true` for `COVERED` or `CONDITIONAL COVERAGE` (see `.insurance[0].item[].excluded` and `.insurance[0].item[].description`) <br> - `false` for `NOT COVERED` |
| `.insurance[0].benefitPeriod.start` | Required | |
| `.insurance[0].benefitPeriod.end` | Optional | |
| `.insurance[0].item[].extension[]` | Required | See [Insurance Item Extensions](#insurance-item-extensions) below. |
| `.insurance[0].item[].category.coding[]` | Required | ??? ToDo -- Complete description after KBS meeting |
| `.insurance[0].item[].category.coding[].system` | Required | ??? ToDo -- Complete description after KBS meeting |
| `.insurance[0].item[].category.coding[].code` | Required | ??? ToDo -- Complete description after KBS meeting |
| `.insurance[0].item[].productOrService` | Required | |
| `.insurance[0].item[].productOrService.coding[]` | Required | Must contain 1 entry. |
| `.insurance[0].item[].productOrService.coding[0].system` | Required | Must be one of `AD` (American Dental Association), `CJ` (CPT), `HC` (HCPCS), `ID` (ICD-9-CM), `N4` (National Drug Code), or `ZZ` (Mutually Defined). |
| `.insurance[0].item[].productOrService.coding[0].code` | Required | 1-48 characters in length. |
| `.insurance[0].item[].modifier[]` | Required | Must contain 1 entry. |
| `.insurance[0].item[].modifier[0].coding[]` | Required | Must contain 1 entry. |
| `.insurance[0].item[].modifier[0].coding[0].system` | Required | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.18003`. |
| `.insurance[0].item[].modifier[0].coding[0].code` | Required | 1-2 characters in length. |
| `.insurance[0].item[].excluded` | Optional | When `.insurance[0].inforce` is `true`, use a `true` value to indicate a `CONDITIONAL COVERAGE`. Use a `false` value or omit to indicate fully `COVERED`. |
| `.insurance[0].item[].description` | Conditional | Required when `.insurance[0].item[].excluded` is `true`. 1-25 characters in length. |
| `.insurance[0].item[].network` | Required | |
| `.insurance[0].item[].network.coding[]` | Required | Must contain 1 entry. |
| `.insurance[0].item[].network.coding[0].system` | Required | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.36580033.8001`.|
| `.insurance[0].item[].network.coding[0].code` | Required | X.12 271 Yes/No code. 1-10 characters in length. |
| `.insurance[0].item[].unit` | Required | |
| `.insurance[0].item[].unit.coding[]` | Required | Must contain 1 entry. |
| `.insurance[0].item[].unit.coding[0].system` | Required | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8003`. |
| `.insurance[0].item[].unit.coding[0].code` | Required | X.12 271 Coverage Level. Must be 3 characters in length. |
| `.insurance[0].item[].term` | Required | |
| `.insurance[0].item[].term.coding[]` | Required | Must contain 1 entry. |
| `.insurance[0].item[].term.coding[0].system` | Required | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8007`. |
| `.insurance[0].item[].term.coding[0].code` | Required | X.12 271 Time Period Qualifier. 1-2 characters in length. |
| `.insurance[0].item[].benefit[].extension[]` | Required | See [Insurance Item Benefit Extensions](#insurance-item-benefit-extensions) below. |
| `.insurance[0].item[].benefit[].type` | Conditional | If a benefit exists, it must have a type. |
| `.insurance[0].item[].benefit[].type.coding[0].system` | Optional | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8002` |
| `.insurance[0].item[].benefit[].type.coding[0].code` | Optional | X.12 271 Eligibility/Benefit code. 0-2 characters in length. |

# ToDo finish everything below this line

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryInfoStatus` | CodeableConcept | Required | Represents the status of the military information known by the payer. Must be 1-10 characters in length. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryEmploymentStatus` | CodeableConcept | Required | Represents the claimants current military employment status. Must be 1-10 characters in length. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryAffiliation` | CodeableConcept | Required | Represents the claimants military affiliation. Must be 1-10 characters in length. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryPersonnelDescription` | String | Required | Free-form text description that further identifies the exact military unit. Must be 1-80 characters in length. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryRank` | CodeableConcept | Required | Represents the claimants military service rank. Must be between 1-10 characters. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-healthCareCode` | Complex | Required | See [Healthcare Code](#healthcare-code) |

### Healthcare Code

### Insurance Extensions

### Insurance Item Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDate` | Complex | Required | See [Subscriber Date](#subscriber-date). |

#### Subscriber Date

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDatePeriod` | Period | Required | |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDateKind` | CodeableConcept | Required | | 

### Insurance Item Benefit Extensions

## Supported Identifiers

- `MSH-10` This is the HL7 message control number that is generated at the time the HL7 message is generated and placed in the outgoing HL7 message queue.
- `MSA-3` This field will contain the Trace Number assigned by EC that is used for tracking a message between EC and the vendor. |Path|Required|Notes| |---|---|---| |`.identifier[].type.text` | Required | Must be `MSH-10` or `MSA-3`. | |`.identifier[0].value`| Required | 1-20 characters when of type `MSH-10`. | |`.identifier[1].value`| Required | 3-15 characters when of type `MSA-3` . |

> Unsupported fields or extensions will be ignored.

Example: [CoverageEligibilityResponse](../vista-fhir-query/samples/coverageEligibilityResponseCreate.json)
