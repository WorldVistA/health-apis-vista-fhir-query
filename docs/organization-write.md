# Organization Write - Insurance Company

> You may only write insurance company organizations.

Supported fields

|Path|Required|Notes|
|---|---|---|
|`.resourceType` | Required | Must be `Organization`. |
|`.id` | Conditional | Should be omitted on create. Must match Organization ID in URL on update. |
| `.identifier[]` | Required | See [Supported Identifiers](#supported-identifiers) below. |
| `.extension[]` | Required | See [Supported Extensions](#supported-extensions) below. |
|`.name` | Required | Name is a key identifier and cannot be updated. |
|`.active` | Required | Must be `true`. |
|`.type[]` | Required | Must contain 1 entry. |
|`.type[0].coding[0].system` | Required | Must be `http://hl7.org/fhir/ValueSet/organization-type` |
|`.type[0].coding[0].code` | Required | Must be `ins` |
|`.address[]` | Required | Must contain 1 entry. |
|`.address[0].line[]` | Required | Must contain 1 to 3 items. |
|`.address[0].line[0]` | Required | 3-35 characters. |
|`.address[0].line[1]` | Optional | 3-30 characters. |
|`.address[0].line[2]` | Optional | 3-30 characters. |
|`.address[0].city` | Optional | 2-25 characters. |
|`.address[0].state` | Optional | 2 characters. |
|`.address[0].postalCode` | Optional | Format `[0-9]{9}` or `[0-9]{5}-[0-9]{4}`. |
ucture)|
|`.contact[]` | Required | See [Supported Contacts](#supported-contacts) below. |
|`.telecom[]` | Required | Must contain a `phone` and `fax` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |
|`.telecom[1].system` | Required | Must be `fax` if second contact point is specified. |
|`.telecom[1].value` | Required | Telephone number, 7-20 characters. Required if second contact point. |

## Supported Contacts

Nine contact numbers are required. Each is determined based on the contact purpose using the `.purpose.coding[0].code` value. Currently, the system is ignored. (TODO https://vajira.max.gov/browse/API-11250)
The following codes are supported. Individual data requirements are described below.

- `APPEAL`
- `BILL`
- `DENTALCLAIMS`
- `INPTCLAIMS`
- `OUTPTCLAIMS`
- `INQUIRY`
- `PRECERT`
- `RXCLAIMS`
- `VERIFY`

### `APPEAL` Appeals Office

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `APPEAL`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Required | Must contain a `phone` contact point. May contain a `fax` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |
|`.telecom[1].system` | Conditional | Must be `fax` if second contact point is specified. |
|`.telecom[1].value` | Conditional | Telephone number, 7-20 characters. Required if second contact point is specified.  |
|`.address.line[]` | Optional | May contain 1 to 3 items. |
|`.address.line[0]` | Optional | 3-35 characters. |
|`.address.line[1]` | Optional | 3-30 characters. |
|`.address.line[2]` | Optional | 3-30 characters. |
|`.address.city` | Optional | 2-25 characters. |
|`.address.state` | Optional | 2 characters. |
|`.address.postalCode` | Optional | Format `[0-9]{9}` or `[0-9]{5}-[0-9]{4}`. |

#### `APPEAL` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

### `BILL` Billing Company

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `BILL`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Required | Must contain a `phone` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |

#### `BILL` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Required | `.display` is used to set the company name. |

### `DENTALCLAIMS` Dental Claims Office

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `DENTALCLAIMS`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Optional | May contain a `phone` and/or contain a `fax` contact point. |
|`.telecom[0].system` | Optional | Must be `phone`. |
|`.telecom[0].value` | Optional | Telephone number, 7-20 characters. |
|`.telecom[1].system` | Optional | Must be `fax` if second contact point is specified. |
|`.telecom[1].value` | Optional | Telephone number, 7-20 characters. Required if second contact point is specified.  |
|`.address.line[]` | Optional | May contain 1 or 2 items. |
|`.address.line[0]` | Optional | 3-35 characters. |
|`.address.line[1]` | Optional | 3-30 characters. |
|`.address.city` | Optional | 2-25 characters. |
|`.address.state` | Optional | 2 characters. |
|`.address.postalCode` | Optional | Format `[0-9]{9}` or `[0-9]{5}-[0-9]{4}`. |

#### `DENTALCLAIMS` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

### `INPTCLAIMS` Inpatient Claims Office

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `INPTCLAIMS`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Required | Must contain a `phone` contact point. May contain a `fax` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |
|`.telecom[1].system` | Conditional | Must be `fax` if second contact point is specified. |
|`.telecom[1].value` | Conditional | Telephone number, 7-20 characters. Required if second contact point is specified.  |
|`.address.line[]` | Optional | May contain 1 to 3 items. |
|`.address.line[0]` | Optional | 3-35 characters. |
|`.address.line[1]` | Optional | 3-30 characters. |
|`.address.line[2]` | Optional | 3-30 characters. |
|`.address.city` | Optional | 2-25 characters. |
|`.address.state` | Optional | 2 characters. |
|`.address.postalCode` | Optional | Format `[0-9]{9}` or `[0-9]{5}-[0-9]{4}`. |

#### `INPTCLAIMS` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

### `OUTPTCLAIMS` Outpatient Claims Office

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `OUTPTCLAIMS`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Required | Must contain a `phone` contact point. May contain a `fax` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |
|`.telecom[1].system` | Conditional | Must be `fax` if second contact point is specified. |
|`.telecom[1].value` | Conditional | Telephone number, 7-20 characters. Required if second contact point is specified.  |
|`.address.line[]` | Optional | May contain 1 to 3 items. |
|`.address.line[0]` | Optional | 3-35 characters. |
|`.address.line[1]` | Optional | 3-30 characters. |
|`.address.line[2]` | Optional | 3-30 characters. |
|`.address.city` | Optional | 2-25 characters. |
|`.address.state` | Optional | 2 characters. |
|`.address.postalCode` | Optional | Format `[0-9]{9}` or `[0-9]{5}-[0-9]{4}`. |

#### `OUTPTCLAIMS` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

### `INQUIRY` Inquiry Office

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `INQUIRY`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Required | Must contain a `phone` contact point. May contain a `fax` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |
|`.telecom[1].system` | Conditional | Must be `fax` if second contact point is specified. |
|`.telecom[1].value` | Conditional | Telephone number, 7-20 characters. Required if second contact point is specified.  |
|`.address.line[]` | Optional | May contain 1 to 3 items. |
|`.address.line[0]` | Optional | 3-35 characters. |
|`.address.line[1]` | Optional | 3-30 characters. |
|`.address.line[2]` | Optional | 3-30 characters. |
|`.address.city` | Optional | 2-25 characters. |
|`.address.state` | Optional | 2 characters. |
|`.address.postalCode` | Optional | Format `[0-9]{9}` or `[0-9]{5}-[0-9]{4}`. |

#### `INQUIRY` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

### `PRECERT` Pre-Certification Office

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `PRECERT`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Required | Must contain a `phone` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |

#### `PRECERT` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

### `RXCLAIMS` Prescription Claims

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `RXCLAIMS`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Optional | May contain a `phone` and/or contain a `fax` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |
|`.telecom[1].system` | Conditional | Must be `fax` if second contact point is specified. |
|`.telecom[1].value` | Conditional | Telephone number, 7-20 characters. Required if second contact point is specified.  |
|`.address.line[]` | Optional | May contain 1 to 3 items. |
|`.address.line[0]` | Optional | 3-35 characters. |
|`.address.line[1]` | Optional | 3-30 characters. |
|`.address.line[2]` | Optional | 3-30 characters. |
|`.address.city` | Optional | 2-25 characters. |
|`.address.state` | Optional | 2 characters. |
|`.address.postalCode` | Optional | Format `[0-9]{9}` or `[0-9]{5}-[0-9]{4}`. |

#### `RXCLAIMS` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

### `VERIFY` Verification Office

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | Ignored at the this time (TODO API-11250). |
|`.purpose.coding[0].code` | Required | Must be `VERIFY`. |
|`.telecom[]` | Required | Must contain a `phone` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |

## Supported Identifiers

Several identifiers are supported.

- Primary payer IDs
- Secondary institutional payer IDs
- Secondary professional payer IDs
- EDI 277 health care status notifications

### Primary Payer IDs

Two primary payer IDs are required and two additional payer IDs may be provided. Each identifier has the same structure.

|Path|Required|Notes|
|---|---|---
|`.type.coding[0].system` | Forbidden | |
|`.type.coding[0].code` | Required | See below. |
|`.value` | Required | |

Supported codes

- `INSTEDI` _(Required)_ Institutional payer ID for claims transmission
- `PROFEDI` _(Required)_ Professional payer ID for claims transmission
- `BIN` _(Optional)_ CHAMPUS fiscal intermediary number for claims transmission
- `DENTALEDI` _(Optional)_ Payer ID for dental claims transmission

### Secondary Institutional and Professions IDs

Up to two secondary institutional payer IDs may be provided. Additionally, up to two secondary professional payer IDs may be provided. Each identifier has the same structure.

|Path|Required|Notes|
|---|---|---
|`.type.coding[0].system` | Required | Secondary payer ID system. See below. |
|`.type.coding[0].code` | Required | Payer qualifier codes. See below. |
|`.value` | Required | |

Secondary institutional payer ID systems

- `urn:oid:2.16.840.1.113883.3.8901.3.1.36.68001` Use this if only supplying one.
- `urn:oid:2.16.840.1.113883.3.8901.3.1.36.68003`

Secondary professional payer IDs systems

- `urn:oid:2.16.840.1.113883.3.8901.3.1.36.68005` Use this if only supplying one.
- `urn:oid:2.16.840.1.113883.3.8901.3.1.36.68007`

Payer Qualifier Codes

- `PAYER ID #`
- `CLAIM OFFICE #`
- `NAIC CODE`
- `FED TAXPAYER #`

Rules

- Identifiers `urn:oid:2.16.840.1.113883.3.8901.3.1.36.68001` and `urn:oid:2.16.840.1.113883.3.8901.3.1.36.68003` cannot have the same code.
- Identifiers `urn:oid:2.16.840.1.113883.3.8901.3.1.36.68005` and `urn:oid:2.16.840.1.113883.3.8901.3.1.36.68007` cannot have the same code.

### EDI 277 Healthcare Status Notifications

EDI 277 identifier is required on create, but forbidden on update.

|Path|Required|Notes|
|---|---|---
|`.type.coding[0].system` | Forbidden | |
|`.type.coding[0].code` | Required | Must be `277EDI`. |
|`.value` | Required | |

# INCOMPLETE BELOW
----

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|

> Unsupported fields or extensions will be ignored.

Example: [Organization](../vista-fhir-query/samples/organizationCreate.json)