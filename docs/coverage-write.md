# Coverage Write

Supported fields

|Path|Required|Notes|
|---|---|---|
|`.resourceType` | Required | Must be `Coverage`. |
|`.id` | Conditional | Must be omitted on create. Must match Coverage ID in URL on update. |
| `.extension[]` | Required | See [Supported Extensions](#supported-extensions) below. |
| `.status` | Required | Must be `active`. |
| `.subscriberId` | Required | |
| `.beneficiary.reference` | Required | Must be full or relative URL to `Patient` resource. |
| `.beneficiary.identifier` | Required | |
| `.beneficiary.identifier.type.coding[0].code` | Required | Must be `MB`. |
| `.beneficiary.identifier.value` | Required | Subscriber's primary ID number. This number is assigned by the payer and can be found on the subscriber's insurance card. |
| `.relationship.coding[]` | Required | Must contain 1 entry. |
| `.relationship.coding[0].system` | Required | Must be `http://terminology.hl7.org/CodeSystem/subscriber-relationship` |
| `.relationship.coding[0].code` | Required | |
| `.period.start` | Required | |
| `.period.end` | Optional | If specified, must be after `.period.start`. |
| `.payor[]` | Required | Must contain 1 entry. |
| `.payor[0].reference` | Required | Must be full or relative URL to an `Organization`. |
| `.order` | Required | Must be `1`, `2`, or `3` for _Primary_, _Secondary_, or _Tertiary_. |
| `.class[]` | Required | Must contain 1 entry. |
| `.class[0].type.coding[0].system` | Required | Must be `http://terminology.hl7.org/CodeSystem/coverage-class`. |
| `.class[0].type.coding[0].code` | Required | Must be `group`. |
| `.class[0].value` | Required | Relative reference to `InsurancePlan`, e.g `InsurancePlan/I3-1JeCN3qnboBvfJAeuA5VVg` |

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverage-pharmacyPersonCode` | `valueInteger` | Optional | This is the code that is assigned by the payer to identify the patient. The payer may use a unique person code to identify each specific person on the pharmacy insurance policy. This code may also describe the patient's relationship to the cardholder. E.g., `1` card holder, `2` spouse, `3` - `999` dependents and others. |
| `http://va.gov/fhir/StructureDefinition/coverage-stopPolicyFromBilling` | `valueBoolean` | Required | Determines whether or not claims may be created for the insurance policy. This field is used primarily for CHAMPUS policies. If the patient is covered under CHAMPUS, but it is known that claims should never be submitted to the CHAMPUS Fiscal Intermediary. A `true` value will prohibit Pharmacy claims submissions to the CHAMPUS Fiscal Intermediary. |

> Unsupported fields or extensions will be ignored.

Example: [Coverage](../vista-fhir-query/samples/coverageCreate.json)
