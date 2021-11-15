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
|`.contact[]` | Required | See [Supported Contacts](#supported-contacts) below. |
|`.telecom[]` | Required | Must contain a `phone` and `fax` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |
|`.telecom[1].system` | Required | Must be `fax` if second contact point is specified. |
|`.telecom[1].value` | Required | Telephone number, 7-20 characters. Required if second contact point. |

## Supported Contacts

Nine contact numbers are required. Each is determined based on the contact purpose using the `.purpose.coding[0].code` value. Currently, the system is ignored. (TODO https://vajira.max.gov/browse/API-11250)
The following codes are supported. Individual data requirements are described below.



<details>
<summary><strong><code>APPEAL</code> Appeals Office</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type`  |
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

`APPEAL` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

</details>
<details>
<summary><strong><code>BILL</code> Billing Company</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type` . |
|`.purpose.coding[0].code` | Required | Must be `BILL`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Required | Must contain a `phone` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |

`BILL` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Required | `.display` is used to set the company name. |

</details>
<details>
<summary><strong><code>DENTALCLAIMS</code> Dental Claims Office</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type` . |
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

`DENTALCLAIMS` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

</details>
<details>
<summary><strong><code>INPTCLAIMS</code> Inpatient Claims Office</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type` . |
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

`INPTCLAIMS` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

</details>
<details>
<summary><strong><code>OUTPTCLAIMS</code> Outpatient Claims Office</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type` . |
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

`OUTPTCLAIMS` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

</details>
<details>
<summary><strong><code>INQUIRY</code> Inquiry Office</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type` . |
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

`INQUIRY` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

</details>
<details>
<summary><strong><code>PRECERT</code> Pre-Certification Office</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type` . |
|`.purpose.coding[0].code` | Required | Must be `PRECERT`. |
|`.extension[]` | Optional | See supported extensions below. |
|`.telecom[]` | Required | Must contain a `phone` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |

`PRECERT` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

</details>
<details>
<summary><strong><code>RXCLAIMS</code> Prescription Claims</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type` . |
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

`RXCLAIMS` Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Optional | `.display` is used to set the company name. |

</details>
<details>
<summary><strong><code>VERIFY</code> Verification Office</strong></summary>

|Path|Required|Notes|
|---|---|---|
|`.purpose.coding[0].system` | Required | *WARNING* System will change https://vajira.max.gov/browse/API-11250. Currently using `http://terminology.hl7.org/CodeSystem/contactentity-type` . |
|`.purpose.coding[0].code` | Required | Must be `VERIFY`. |
|`.telecom[]` | Required | Must contain a `phone` contact point. |
|`.telecom[0].system` | Required | Must be `phone`. |
|`.telecom[0].value` | Required | Telephone number, 7-20 characters. |

</details>

----

## Supported Identifiers

Several identifiers are supported.

<details>
<summary><strong> Primary Payer IDs</strong></summary>

Two primary payer IDs are required and two additional payer IDs may be provided. Each identifier has the same structure.

|Path|Required|Notes|
|---|---|---
|`.type.coding[0].system` | Forbidden | There is no system for primary payer ID codes. |
|`.type.coding[0].code` | Required | See below. |
|`.value` | Required | |

Supported codes

- `INSTEDI` _(Required)_ Institutional payer ID for claims transmission
- `PROFEDI` _(Required)_ Professional payer ID for claims transmission
- `BIN` _(Optional)_ CHAMPUS fiscal intermediary number for claims transmission
- `DENTALEDI` _(Optional)_ Payer ID for dental claims transmission

</details>
<details>
<summary><strong>Secondary Institutional and Professions IDs</strong></summary>

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

</details>
<details>
<summary><strong>EDI 277 Healthcare Status Notifications</strong></summary>

EDI 277 identifier is required on create, but forbidden on update.

|Path|Required|Notes|
|---|---|---
|`.type.coding[0].system` | Forbidden | |
|`.type.coding[0].code` | Required | Must be `277EDI`. |
|`.value` | Required | |

</details>

----

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/organization-allowMultipleBedsections` | `valueBoolean` | Optional | `true` indicates the insurance company will accept multiple bedsections on one claim form. If `false`, then only the first bedsection in the date range will be used when submitting claims. |
| `http://va.gov/fhir/StructureDefinition/organization-oneOutpatVisitOnBillOnly` | `valueBoolean` | Optional |`true` indicates only one outpatient visit will be allowed per claim form for the insurance company. If `false`, then multiple (up to 10) outpatient bills will be allowed per claim form. |
| `http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesInpatClaims` | `valueBoolean` | Optional | `true` indicates another insurance company processes inpatient claims.|
| `http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesAppeals` | `valueBoolean` | Optional | `true` indicates another insurance company processes appeals. |
| `http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesInquiries` | `valueBoolean` | Optional | `true` indicates another insurance company processes inquiries. |
| `http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesOutpatClaims` | `valueBoolean` | Optional | `true` indicates another insurance company processes outpatient claims. |
| `http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesPrecert` | `valueBoolean` | Optional | `true` indicates another insurance company processes precerts. |
| `http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesRxClaims` | `valueBoolean` | Optional | `true` indicates another insurance company processes prescription claims. |
| `http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesDentalClaims` | `valueBoolean` | Optional | `true` indicates another insurance company processes dental claims. |
| `http://va.gov/fhir/StructureDefinition/organization-signatureRequiredOnBill` | `valueBoolean` | Required | `true` indicates a signature is required on a bill before being submitted to the insurance carrier. |
| `http://va.gov/fhir/StructureDefinition/organization-attendingRenderingProviderSecondaryIDProfesionalRequired` | `valueBoolean` | Required | `true` indicates the insurance company wishes to have the attending/rendering provider secondary ID used as a billing provider secondary ID. This applies to CMS-1500 claims. |
| `http://va.gov/fhir/StructureDefinition/organization-attendingRenderingProviderSecondaryIDInstitutionalRequired` | `valueBoolean` | Required | `true` indicates the insurance company wishes to have the attending/rendering provider secondary ID used as a billing provider secondary ID. This applies to UB claims. |
| `http://va.gov/fhir/StructureDefinition/organization-printSecTertAutoClaimsLocally` | `valueBoolean` | Required | `true` indicates automatically-processed secondary or tertiary claims to this payer must be printed locally. |
| `http://va.gov/fhir/StructureDefinition/organization-printSecMedClaimsWOMRALocally` | `valueBoolean` | Required | `true` indicates secondary Medicare claims to this payer which have not been transmitted to Medicare and for which no MRA has been received, must be printed locally. |
| `http://va.gov/fhir/StructureDefinition/organization-ambulatorySurgeryRevenueCode` | `valueCodeableConcept` | Optional | `.coding[0].system` is `urn:oid:2.16.840.1.113883.6.301.3`. `.coding[0].value` is the revenue code that will automatically be generated for this insurance company if a billable ambulatory surgical code is listed as a procedure in this this bill. | 
| `http://va.gov/fhir/StructureDefinition/organization-prescriptionRevenueCode` | `valueCodeableConcept` | Optional | `.coding[0].system` is `urn:oid:2.16.840.1.113883.6.301.3`. `.coding[0].value` is the revenue code that will automatically be generated for this insurance company if a prescription refill is listed on this bill. | 
| `http://va.gov/fhir/StructureDefinition/organization-typeOfCoverage` | `valueCodeableConcept` | Required | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.36.8013`. `.coding[0].value` is _Type of Coverage Codes_ defined below. | 
| `http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare` | `valueCodeableConcept` | Required | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.1.36.1`. `.coding[0].value` is _Will Reimburse For Care Codes_ defined below. | 
| `http://va.gov/fhir/StructureDefinition/organization-electronicTransmissionMode` | `valueCodeableConcept` | Required | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.1.36.38001`. `.coding[0].value` is `YES-LIVE` for production claims or `YES-TEST` for test claims. | 
| `http://va.gov/fhir/StructureDefinition/organization-electronicInsuranceType` | `valueCodeableConcept` | Required | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.1.36.38009`. `.coding[0].value` identifies the type of insurance company and is one of `COMMERCIAL`, `GROUP POLICY`, `HMO`, `MEDICAID`, `MEDICARE`, `OTHER`. | 
| `http://va.gov/fhir/StructureDefinition/organization-performingProviderSecondIDTypeCMS1500` | `valueCodeableConcept` | Optional | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001`. `.coding[0].value` is _IB Provider ID Number Type Codes_ defined below. | 
| `http://va.gov/fhir/StructureDefinition/organization-performingProviderSecondIDTypeUB04` | `valueCodeableConcept` | Optional | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001`. `.coding[0].value` is _IB Provider ID Number Type Codes_ defined below. | 
| `http://va.gov/fhir/StructureDefinition/organization-referringProviderSecondIDTypeCMS1500` | `valueCodeableConcept` | Optional | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001`. `.coding[0].value` is _IB Provider ID Number Type Codes_ defined below. | 
| `http://va.gov/fhir/StructureDefinition/organization-referringProviderSecondIDTypeUB04` | `valueCodeableConcept` | Optional | `.coding[0].system` is `urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001`. `.coding[0].value` is _IB Provider ID Number Type Codes_ defined below. | 
| `http://va.gov/fhir/StructureDefinition/organization-filingTimeFrame` | `valueString` | Required | Maximum amount of time from date of service that the insurance company allows for submitting claims. Answer must be 3-30 characters in length. Examples: 90 days, 6 months, 1 year, 18 months, March 30 after year of service.| 
| `http://va.gov/fhir/StructureDefinition/organization-planStandardFilingTimeFrame` | `valueQuantity` | Required | `.system` is `urn:oid:2.16.840.1.113883.3.8901.3.3558013`. `.unit` is _Insurance Filing Time Frame Codes_ defined below. `.value` is number in declared units. | 
| `http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary` | `valueReference` | Required | `.reference` is a relative Organization reference to the payer, e.g., `Organization/I3-450NAk1LKUAXKzQ35A62U1`  | 

<details>
<summary><strong>Type of Coverage Codes</strong></summary>

System `urn:oid:2.16.840.1.113883.3.8901.3.36.8013`

Used with

- `http://va.gov/fhir/StructureDefinition/organization-typeOfCoverage`

If this insurance carrier provides only one type of coverage then choose the value that best describes this carriers type of coverage. If this carrier provides more than one type of coverage then use `HEALTH INSURANCE`.

- `BLUE CROSS`
- `BLUE SHIELD`
- `CHAMPVA`
- `DISABILITY INCOME INSURANCE`
- `HEALTH INSURANCE`
- `HEALTH MAINTENANCE ORG.`
- `INDEMNITY`
- `MEDI-CAL`
- `MEDICAID`
- `MEDICARE`
- `MEDIGAP`
- `MENTAL HEALTH ONLY`
- `PRESCRIPTION ONLY`
- `SUBSTANCE ABUSE ONLY`
- `TORT/FEASOR`
- `TRICARE`
- `VA SPECIAL CLASS`
- `WORKERS' COMPENSATION`

> Unsupported fields or extensions will be ignored.

Example: [Organization](../vista-fhir-query/samples/organizationCreate.json)

### Will Reimburse For Care Codes

System `urn:oid:2.16.840.1.113883.3.8901.3.1.36.1`

Used with

- `http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare`

This code denotes under which circumstances this insurance carrier will reimburse the Dept. of Veterans Affairs for care received.

- `REIMBURSE`
- `WILL REIMBURSE IF TREATED UNDER VAR 6046(C) OR VAR 6060.2(A)`
- `DEPENDS ON POLICY, CHECK WITH COMPANY`
- `WILL NOT REIMBURSE`

</details>
<details>
<summary><strong>IB Provider ID Number Type Codes</strong></summary>

System `urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001`

Used with

- `http://va.gov/fhir/StructureDefinition/organization-performingProviderSecondIDTypeCMS1500`
- `http://va.gov/fhir/StructureDefinition/organization-performingProviderSecondIDTypeUB04`
- `http://va.gov/fhir/StructureDefinition/organization-referringProviderSecondIDTypeCMS1500`
- `http://va.gov/fhir/StructureDefinition/organization-referringProviderSecondIDTypeUB04`

This is the type of performing provider secondary id that the insurance company expects on CMS-1500 or UB-04 bills received from VA. When the payer-specific provider id is extracted, this field is used to determine where to get the default data from if another secondary id is not entered for the claim.

- `BLUE CROSS`
- `BLUE SHIELD`
- `CHAMPUS`
- `CLIA #`
- `CLINIC NUMBER`
- `COMMERCIAL`
- `DEA #`
- `EIN`
- `ELECTRONIC PLAN TYPE`
- `EMC ID`
- `EMPLOYER'S IDENTIFICATION #`
- `FEDERAL TAXPAYER'S #`
- `HMO`
- `LOCATION NUMBER`
- `MEDICAID`
- `MEDICARE PART A`
- `MEDICARE PART B`
- `NATIONAL PROVIDER ID`
- `PPO NUMBER`
- `PROVIDER PLAN NETWORK`
- `PROVIDER SITE NUMBER`
- `SOCIAL SECURITY NUMBER`
- `STATE INDUSTRIAL ACCIDENT PROV`
- `STATE LICENSE`
- `UPIN`
- `USIN`

</details>
<details>
<summary><strong>Insurance Filing Time Frame Codes</strong></summary>

System `urn:oid:2.16.840.1.113883.3.8901.3.3558013`

Used with

- `http://va.gov/fhir/StructureDefinition/organization-planStandardFilingTimeFrame`

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

----

Example: [Organization](../vista-fhir-query/samples/organizationCreate.json)
