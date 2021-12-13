# CoverageEligibilityResponse Write

## Supported Fields

|Path|Required|Notes|
|---|---|---|
| `.resourceType` | Required | Must be `CoverageEligibilityResponse`. |
| `.id` | Conditional | Should be omitted on create. |
| `.extension[]` | Required | See [Supported Extensions](#supported-extensions) below. |
| `.identifier[]` | Required | See [Supported Identifiers](#supported-identifiers) below. |
| `.status` | Required | Must be `active`. |
| `.purpose[]` | Required | Must contain 1 entry. |
| `.purpose[0]` | Required | Must be `benefits`. |
| `.patient.reference` | Required | Must be a full or relative url to the `Patient` resource. |
| `.servicedDate` | Required | |
| `.created` | Required | |
| `._request` | Required | Must be a data absent reason extension. |
| `.outcome` | Required | Must be `complete`. |
| `.insurer.reference` | Required | Must be a full or relative url to an `Organization` with type `pay`. |
| `.insurance[]` | Required | ToDo https://vajira.max.gov/browse/API-11395 -- come back and reevaluate this statement after KBS feedback -- Must contain 1 entry. |
| `.insurance[0].extension[]` | Required | See [Insurance Extensions](#insurance-extensions) below. |
| `.insurance[0].coverage.reference` | Required | Must be a full or relative url to the `Coverage` resource. |
| `.insurance[0].inforce` | Required | - `true` for `COVERED` or `CONDITIONAL COVERAGE` (see `.insurance[0].item[].excluded` and `.insurance[0].item[].description`) <br> - `false` for `NOT COVERED` |
| `.insurance[0].benefitPeriod.start` | Required | |
| `.insurance[0].benefitPeriod.end` | Optional | |
| `.insurance[0].item[].extension[]` | Required | See [Insurance Item Extensions](#insurance-item-extensions) below. |
| `.insurance[0].item[].category.coding[]` | Required | ToDo https://vajira.max.gov/browse/API-11395 -- Complete description after KBS meeting |
| `.insurance[0].item[].category.coding[].system` | Required | ToDo https://vajira.max.gov/browse/API-11395 -- Complete description after KBS meeting |
| `.insurance[0].item[].category.coding[].code` | Required | ToDo https://vajira.max.gov/browse/API-11395 -- Complete description after KBS meeting |
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
| `.insurance[0].item[].benefit[].allowedMoney` | Conditional | If a benefit exists, it must have either `allowedMoney` or `usedMoney` specified but not both. |
| `.insurance[0].item[].benefit[].usedMoney` | Conditional | If a benefit exists, it must have either `allowedMoney` or `usedMoney` specified but not both. |
| `.insurance[0].item[].benefit[].type.coding[0].system` | Optional | Must be `urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8002` |
| `.insurance[0].item[].benefit[].type.coding[0].code` | Optional | X.12 271 Eligibility/Benefit code. 0-2 characters in length. |
| `.insurance[0].item[].authorizationRequired` | Required | |

## Supported Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryInfoStatus` | `valueCodeableConcept` | Required | `.coding[0].system` must be `urn:oid:2.16.840.1.113883.3.8901.3.1.365.128001`. `.coding[0].value` must be `A` (Partial), `C` (Current), `L` (Latest), `P` (Prior), `S` (Second Most Current), `T` (Third Most Current) to indicate the status of the military information known by the payer. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryEmploymentStatus` | `valueCodeableConcept` | Required | `.coding[0].system` must be `urn:oid:2.16.840.1.113883.3.8901.3.1.365.128002`. `.coding[0].value` is _Military Employment Status Code_ described below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryAffiliation` | `valueCodeableConcept` | Required | `.coding[0].system` must be `urn:oid:2.16.840.1.113883.3.8901.3.1.365.128003`. `.coding[0].value` is _Military Affiliation Code_ described below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryPersonnelDescription` | `valueString` | Required | Free-form text description that further identifies the exact military unit. Must be 1-80 characters in length. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryRank` | `valueCodeableConcept` | Required | `.coding[0].system` must be `urn:oid:2.16.840.1.113883.3.8901.3.1.365.128005`. `.coding[0].value` is _Military Rank Code_ described below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-healthCareCode` | Complex | Required | See [Healthcare Code](#healthcare-code) |

<details><summary><strong>Military Employment Status Code</strong></summary>
This code indicates the claimant's current military employment status.

- `AE` (Active Reserve)
- `AO` (Active Military-Overseas)
- `AS` (Academy Student)
- `AT` (Presidential Appointee)
- `AU` (Active Military-USA)
- `CC` (Contractor)
- `DD` (Dishonorably Discharged)
- `HD` (Honorably Discharged)
- `IR` (Inactive Reserves)
- `LX` (Leave Of Absence: Military)
- `PE` (Plan To Enlist)
- `RE` (Recommissioned)
- `RM` (Retired Military-Overseas)
- `RR` (Retired Without Recall)
- `RU` (Retired Military-USA)

</details>


<details><summary><strong>Military Affiliation Code</strong></summary>

This code indicates the claimant's military affiliation

- `A` (Air Force)
- `B` (Air Force Reserves)
- `C` (Army)
- `D` (Army Reserves)
- `E` (Coast Guard)
- `F` (Marine Corps)
- `G` (Marine Corps Reserves)
- `H` (National Guard)
- `I` (Navy)
- `J` (Navy Reserves)
- `K` (Other)
- `L` (Peace Corps)
- `M` (Regular Armed Forces)
- `N` (Reserves)
- `O` (U.S. Public Health Service)
- `Q` (Foreign Military)
- `R` (American Red Cross)
- `S` (Department Of Defense)
- `U` (United Services Organization)
- `W` (Military Sealift Command)

</details>


<details><summary><strong>Military Rank Code</strong></summary>

This code indicates the claimant's military rank.

- `A1` (Admiral)
- `A2` (Airman)
- `A3` (Airman First Class)
- `B1` (Basic Airman)
- `B2` (Brigadier General)
- `C1` (Captain)
- `C2` (Chief Master Sergeant)
- `C3` (Chief Petty Officer)
- `C4` (Chief Warrant)
- `C5` (Colonel)
- `C6` (Commander)
- `C7` (Commodore)
- `C8` (Corporal)
- `C9` (Corporal Specialist 4)
- `E1` (Ensign)
- `F1` (First Lieutenant)
- `F2` (First Sergeant)
- `F3` (First Sergeant-Master Sergeant)
- `F4` (Fleet Admiral)
- `G1` (General)
- `G4` (Gunnery Sergeant)
- `L1` (Lance Corporal)
- `L2` (Lieutenant)
- `L3` (Lieutenant Colonel)
- `L4` (Lieutenant Commander)
- `L5` (Lieutenant General)
- `L6` (Lieutenant Junior Grade)
- `M1` (Major)
- `M2` (Major General)
- `M3` (Master Chief Petty Officer)
- `M4` (Master Gunnery Sergeant Major)
- `M5` (Master Sergeant)
- `M6` (Master Sergeant Specialist 8)
- `P1` (Petty Officer First Class)
- `P2` (Petty Officer Second Class)
- `P3` (Petty Officer Third Class)
- `P4` (Private)
- `P5` (Private First Class)
- `R1` (Rear Admiral)

</details>

#### Healthcare Code

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `diagnosisCode` | `valueCodeableConcept` | Required | `.coding[0].system` must be one of  `http://hl7.org/fhir/sid/icd-9-cm`, `http://www.cms.gov/Medicare/Coding/ICD9","http://hl7.org/fhir/sid/icd-10-cm`, `http://www.cms.gov/Medicare/Coding/ICD10`. `.coding[0].code` must be a corresponding ICD code. |
| `diagnosisCodeQualifier` | `valueString` | Required | ToDo https://vajira.max.gov/browse/API-11395 verify allowed values from KBS.  |
| `primaryOrSecondary` | `valueString` | Required | `P` for Primary or `S` for secondary. | 

### Insurance Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-effectiveDate` | `valueDateTime` | Required |  |


### Insurance Item Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-healthcareServicesDelivery` | Complex | Required | See [Healthcare Services Delivery](#healthcare-services-delivery). |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberAdditionalInfo` | Complex | Required | See [Subscriber Additional Info](#subscriber-additional-info). |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDate` | Complex | Required | See [Subscriber Date](#subscriber-date). |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceId` | Complex | Required | See [Subscriber Reference Id](#subscriber-reference-id). |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-coverageCategory` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.3558032.8003`. `.coding[0].value` must be _Coverage Category Code_ defined below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-requestedServiceType` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.280312.888002`. `.coding[0].value` must be _Requested Service Type Code_ defined below. |

<details><summary><strong>Category Coverage Code</strong></summary>


The type of coverage that the insurance company plan has reimbursement limits on.

- `DENTAL`
- `INPATIENT`
- `LONG TERM CARE`
- `MENTAL HEALTH`
- `OUTPATIENT`
- `PHARMACY`
- `PROSTHETICS`

</details>

<details><summary><strong>Requested Service Type Code</strong></summary>

X.12 271 EB04 (Insurance Type)

- `12` (Medicare 2ndary Working Aged Beneficiary or Spouse with Employer GHP)
- `13` (Medicare 2ndary ESRD Beneficiary in the 12 month coordination period with Employer GHP)
- `14` (Medicare 2ndary, No-fault Insurance including Auto is Primary)
- `15` (Medicare 2ndary Workers Compensation)
- `16` (Medicare 2ndary PHS or Other Federal Agency)
- `41` (Medicare 2ndary Black Lung)
- `42` (Medicare 2ndary Veterans Admin)
- `43` (Medicare 2ndary Disabled Beneficial <65 with Large LGHP)
- `47` (Medicare 2ndary, Other Liability Insurance is Primary)
- `AP` (Auto Insurance Policy)
- `C1` (Commercial)
- `CO` (COBRA)
- `CP` (Medicare Conditionally Primary)
- `D` (Disability)
- `DB` (Disability Benefits)
- `EP` (Exclusive Provider Organization)
- `FF` (Family or Friends)
- `GP` (Group Policy)
- `HM` (HMO)
- `HN` (HMO - Medicare Risk)
- `HS` (Special Low Income Medicare Beneficiary)
- `IN` (Indemnity)
- `IP` (Individual Policy)
- `LC` (Long Term Care)
- `LD` (Long Term Policy)
- `LI` (Life Insurance)
- `LT` (Litigation)
- `MA` (Medicare Part A)
- `MB` (Medicare Part B)
- `MC` (Medicaid)
- `MH` (Medigap Part A)
- `MI` (Medigap Part B)
- `MP` (Medicare Primary)
- `OT` (Other)
- `PE` (Property Insurance - Personal)
- `PL` (Personal)
- `PP` (Personal Payment/Cash - No Insurance)
- `PR` (PPO)
- `PS` (Point of Service (POS))
- `QM` (Qualified Medicare Beneficiary)
- `RP` (Property Insurance - Real)
- `SP` (Supplemental Policy)
- `TF` (TEFRA)
- `WC` (Workers Compensation)
- `WU` (Wrap Up Policy)

</details>

#### Healthcare Services Delivery

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-benefitQuantity` | `valueQuantity` | Required | `.value` must be a number between 0 and 99999 with up to 3 decimal digits. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-quantityQualifier` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.3658027.8003`. `.coding[0].value` must be _Quantity Qualifier Code_ defined below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-sampleSelectionModulus` | `valueString` | Required | Sampling frequency. Must be 1-6 characters in length. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-unitsOfMeasurement` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.3658027.8005`. `.coding[0].value` must be one of `DA` (Days), `MO` (Months), `VS` (Visits), `WK` (Weeks), `YR` (Years) to indicate the X.12 271 Units Of Measurement. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-timePeriods` | `valueDecimal` | Required | The number of time periods as qualified by the `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-timePeriodQualifier` extension. Must be a number between 0 and 99999 with at most 2 decimal digits. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-timePeriodQualifier` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.3658027.8007`. `.coding[0].value` must be _Time Period Qualifier Code_ defined below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-deliveryFrequency` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.3658027.8008`. `.coding[0].value` must be _Delivery Frequency Code_ defined below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-deliveryPattern` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.3658027.8009`. `.coding[0].value` must be _Delivery Pattern Code_ defined below. |


<details><summary><strong>Quantity Qualifier Code</strong></summary>

X.12 271 Quantity Qualifier code

- `99` (Quantity Used)
- `8H` (Minimum)
- `CA` (Covered/Actual)
- `CE` (Covered/Estimated)
- `D3` (Number Of Co-insurance Days)
- `DB` (Deduct. Blood Units)
- `DY` (Days)
- `FL` (Units)
- `HS` (Hours)
- `LA` (Lifetime Reserve/Actual)
- `LE` (Lifetime Reserve/Est)
- `M2` (Maximum)
- `MN` (Month)
- `P6` (Number of Serv/Proc)
- `QA` (Quantity Approved)
- `S7` (Age/High Value)
- `S8` (Age/Low Value)
- `VS` (Visits)
- `YY` (Years)

</details>

<details><summary><strong>Time Period Qualifier Code</strong></summary>

X.12 271 Time Period Qualifier

- `6` (Hour)
- `7` (Day)
- `13` (24 Hours)
- `21` (Years)
- `22` (Service Year)
- `23` (Calendar Year)
- `24` (Year to Date)
- `25` (Contract)
- `26` (Episode)
- `27` (Visit)
- `28` (Outlier)
- `29` (Remaining)
- `30` (Exceeded)
- `31` (Not Exceeded)
- `32` (Lifetime)
- `33` (Lifetime Remaining)
- `34` (Month)
- `35` (Week)
- `36` (Admission)

</details>

<details><summary><strong>Delivery Frequency Code</strong></summary>

X.12 271 Delivery Frequency Code.

- `1` (1st Wk of Month)
- `2` (2nd Wk of the Month)
- `3` (3rd Wk of Month)
- `4` (4th Wk of Month)
- `5` (5th Wk of Month)
- `6` (1st & 3rd Wk of Month)
- `7` (2nd & 4th Wk of Month)
- `8` (1st Working Day of Period)
- `9` (Last Working Day of Period)
- `A` (Mon. through Fri.)
- `B` (Mon. through Sat.)
- `C` (Mon. through Sun.)
- `D` (Mon.)
- `E` (Tues.)
- `F` (Wed.)
- `G` (Thurs.)
- `H` (Fri.)
- `J` (Sat.)
- `K` (Sun.)
- `L` (Mon. through Thurs.)
- `M` (Immediately)
- `N` (As Directed)
- `O` (Daily Mon. through Fri.)
- `P` (1/2 Mon., 1/2 Thurs.)
- `Q` (1/2 Tues., 1/2 Thurs.)
- `R` (1/2 Wed., 1/2 Fri.)
- `S` (Once Anytime Mon. through Fri.)
- `SA` (Sun., Mon., Thurs., Fri., Sat.)
- `SB` (Tue., through Sat.)
- `SC` (Sun., Wed., Thurs., Fri., Sat.)
- `SD` (Mon., Wed., Thurs., Fri., Sat.)
- `SG` (Tues. through Fri.)
- `SL` (Mon., Tues., Thurs.)
- `SP` (Mon., Tues., Fri.)
- `SX` (Wed., Thurs.)
- `SY` (Mon., Wed., Thurs.)
- `SZ` (Tues., Thurs., Fri.)
- `T` (1/2 Tue., 1/2 Fri.)
- `U` (1/2 Mon., 1/2 Wed.)
- `V` (1/3 Mon., 1/3 Wed., 1/3 Fri.)
- `W` (Whenever Necessary)
- `WE` (Weekend)
- `X` (1/2 By Wed., Bal. By Fri.)
- `Y` (None)
</details>

<details><summary><strong>Delivery Pattern Code</strong></summary>

X.12 271 Delivery Pattern.

- `A` (1st Shift)
- `B` (2nd Shift)
- `C` (3rd Shift)
- `D` (A.M.)
- `E` (P.M.)
- `F` (As Directed)
- `G` (Any Shift)
- `Y` (None)

</details>

#### Subscriber Additional Info

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberPlaceOfService` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.2.3658029.8002`. `.coding[0].value` must be _Subscriber Place of Service Code_ defined below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberQualifier` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.2.3658029.8004`. `.coding[0].value` must be _Subscriber Qualifier Code_ defined below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryCode` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.2.3658029.8005`. `.coding[0].value` must be X.12 271 Nature Of Injury Codes. For example: `GR` for Nature Of Injury (NCCI) or `NI` for Nature of Injury . |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryCategory` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.2.3658029.8006`. `.coding[0].value` must be an X.12 271 Injury Category code. For example: `44` (Nature Of Injury). |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryText` | `valueString` | Required | Must be 1-80 characters in length. |

<details><summary><strong>Subscriber Place of Service Code</strong></summary>

- `01` (Pharmacy)
- `02` (Telehealth-Locat-Hlth Svcs/Related Svcs Telec Sys)
- `03` (School)
- `04` (Homeless Shelter)
- `05` (Ihs Free Standing Facility)
- `06` (Ihs Provider Based Facility)
- `07` (Tribal 638 Free Standing Facility)
- `08` (Tribal 638 Provider Based Facility)
- `09` (Prison Correctional Facility)
- `11` (Office)
- `12` (Home)
- `13` (Assisted Living Facility)
- `14` (Group Home)
- `15` (Mobile Unit)
- `16` (Temporary Lodging)
- `17` (Walk-In Retail Health Clinic)
- `18` (Place Of Employment-Worksite)
- `19` (Off Campus-Outpatient Hospital)
- `20` (Urgent Care Facility)
- `21` (Inpatient Hospital)
- `22` (On Campus-Outpatient Hospital)
- `23` (Emergency Room - Hospital)
- `24` (Ambulatory Surgical Center)
- `25` (Birthing Center)
- `26` (Military Treatment Facility)
- `31` (Skilled Nursing Facility)
- `32` (Nursing Facility)
- `33` (Custodial Care Facility)
- `34` (Hospice)
- `41` (Ambulance - Land)
- `42` (Ambulance - Air Or Water)
- `49` (Independent Clinic)
- `50` (Federally Qualified Health Center)
- `51` (Inpatient Psychiatric Facility)
- `52` (Psy. Fac. Partial Hospitalization)
- `53` (Community Mental Health Center)
- `54` (Intermediate Care Fac/Mentally Retarded)
- `55` (Residential Substance Abuse Treatment Facility)
- `56` (Psychiatric Residential Treatment Center)
- `57` (Non Residential Substance Abuse Treatment Facility)
- `60` (Mass Immunization Center)
- `61` (Comprehensive Inpatient Rehabilitation Facility)
- `62` (Comprehensive Outpatient Rehabilitation Facility)
- `65` (End Stage Renal Disease Treatment Facility)
- `71` (State Or Local Public Health Clinic)
- `72` (Rural Health Clinic)
- `81` (Independent Laboratory)
- `99` (Other Place Of Service)

</details>

<details><summary><strong>Subscriber Qualifier Code</strong></summary>

X.12 271 Code List Qualifier.

- `BF` (Diagnosis)
- `BK` (Principal Diagnosis)
- `GR` (National Council on Compensation Insurance (NCCI) Nature of Injury Code)
- `NI` (Nature of Injury Code)
- `ZZ` (Place of Service)

</details>

#### Subscriber Date

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDatePeriod` | `valuePeriod` | Required | `.start` is required `.end` is optional. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDateKind` | `valueCodeableConcept` | Required | `.coding[0].system` must be `urn:oid:2.16.840.1.113883.3.8901.3.1.36580288.8003`. `.coding[0].value` must be _Date Qualifier Code_ defined below. | 

<details><summary><strong>Subscriber Date Qualifier Code</strong></summary>

X.12 271 Date Qualifier Code

- `102` (Issue)
- `152` (Effective Date Of Change)
- `193` (Period Start)
- `194` (Period End)
- `198` (Completion)
- `290` (COB)
- `291` (Plan)
- `292` (Benefit)
- `295` (PCP)
- `304` (Latest Visit/Consultation)
- `307` (Eligibility)
- `318` (Added)
- `340` (COBRA Begin)
- `341` (COBRA End)
- `342` (Premium Paid To Date Begin)
- `343` (Premium Paid To Date End)
- `346` (Plan Begin)
- `347` (Plan End)
- `348` (Benefit Begin)
- `349` (Benefit End)
- `356` (Eligibility Begin)
- `357` (Eligibility End)
- `382` (Enrollment)
- `435` (Admission)
- `442` (Date Of Death)
- `458` (Certification)
- `472` (Service)
- `539` (Policy Effective)
- `540` (Policy Expiration)
- `636` (Date Last Update)
- `771` (Status)
- `096` (Discharge)

</details>

#### Subscriber Reference Id

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdValue` | `valueString` | Required | 1-30 characters in length. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdQualifier` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.36580291.8003`. `.coding[0].value` must be X.12 271 Reference Identification Qualifier Code defined below. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdDescription` | `valueString` | Required | 1-80 characters in length. |

<details><summary><strong>Subscriber Reference ID Qualifier Code</strong></summary>

X.12 271 Reference Identification Qualifier Code

- `18` (Plan Number)
- `49` (Family Unit Number)
- `1L` (Group or Policy Number)
- `1W` (Member Identification Number)
- `6P` (Group Number)
- `9F` (Referral Number)
- `9K` (Servicer)
- `A6` (Employee Identification Number)
- `D3` (National Association Of Boards Of Pharmacy Number)
- `EI` (EIN)
- `F6` (Health Insurance Claim (HIC) Number)
- `G1` (Prior Authorization Number)
- `HP` (Healthcare Financing Administration)
- `IG` (Insurance Policy Number)
- `N6` (Plan Network Identification Number)
- `NQ` (Medicaid Recipient Identification Number)
- `PX` (Health Care Provider Taxonomy Code)
- `SY` (SSN)
- `TJ` (Federal Tax Id)
- `ZZ` (Taxonomy cody)

</details>

### Insurance Item Benefit Extensions

| Defining URL | Type | Required | Notes |
|---|---|---|---|
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-benefitQuantity` | `valueQuantity` | Required | X.12 271 EB10 code. Quantity value as qualified by the quantity code. 1-15 characters in length. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-benefitQuantityCode` | `valueCodeableConcept` | Required | `.coding[0].system` must be `2.16.840.1.113883.3.8901.3.1.3658002.801`. `.coding[0].value` must be an X.12 271 EB09 unit. |
| `http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-benefitPercent` | `valueQuantity` | Required |  X.12 271 EB08 value. Benefit qualified by percentage. Must be a number between 0 and 100, 5 Decimal Digits. |

<details>
  <summary>
    <strong>Benefit Quantity Code</strong>
  </summary>

X12 271 EB09 (Quantity Qualifier)

- `99` (Quantity Used)
- `8H` (Minimum)
- `CA` (Covered/Actual)
- `CE` (Covered/Estimated)
- `D3` (Number Of Co-insurance Days)
- `DB` (Deduct. Blood Units)
- `DY` (Days)
- `FL` (Units)
- `HS` (Hours)
- `LA` (Lifetime Reserve/Actual)
- `LE` (Lifetime Reserve/Est)
- `M2` (Maximum)
- `MN` (Month)
- `P6` (Number of Serv/Proc)
- `QA` (Quantity Approved)
- `S7` (Age/High Value)
- `S8` (Age/Low Value)
- `VS` (Visits)
- `YY` (Years)

</details>

## Supported Identifiers

- `MSH-10` This is the HL7 message control number that is generated at the time the HL7 message is generated and placed in the outgoing HL7 message queue.
- `MSA-3` This field will contain the Trace Number assigned by EC that is used for tracking a message between EC and the vendor.

| Path | Required | Notes | 
|---|---|--- 
|`.identifier[].type.text` | Required | Must be `MSH-10` or `MSA-3`. | 
|`.identifier[0].value`| Required | 1-20 characters when of type `MSH-10`. | 
|`.identifier[1].value`| Required | 3-15 characters when of type `MSA-3` . |

## Steps for creating a new CoverageEligibilityResponse
1. Create a new InsurancePlan entry by using a new/unique value in the Group Number field (first entry in the identifier list).
2. Create a new Coverage entry by referencing the new InsurancePlan entry in the Coverage's `.class[0].value` field.
3. Create a new CoverageEligibilityResponse. The new Coverage must be referenced in the CoverageEligibilityResponse's `.insurance[0].coverage.reference` field. The MSH-10 `.value` field must be unique.

> Unsupported fields or extensions will be ignored.

Example: [CoverageEligibilityResponse](../vista-fhir-query/samples/coverageEligibilityResponseCreate.json)
