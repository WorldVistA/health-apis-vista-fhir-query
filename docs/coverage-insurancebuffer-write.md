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
| `.identifier[]` | Required | See [Supported Identifiers](#supported-identifiers) below. |
| `.name` | Optional | This is the name that the insurance company uses to identify the plan. |

#### Supported Identifiers

Several identifiers are supported.
<details>
<summary><strong>Group Number</strong></summary>

| Path | Required | Notes |
|---|---|---
| `.system` | Required | `urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.908002` |
| `.value` | Required | If this is a group policy enter the number which identifies this policy, i.e. group number/code that the insurance company uses to identify this  plan. Must be unique. |

</details>

### Organization

#### Supported Fields

|Path|Required|Notes|
|---|---|---|
|`.resourceType` | Required | Must be `Organization`. |
| `.id` | Required | Must match the id provided in the `Coverage` `.payor[0]` field. |
| `.name` | Required | |



