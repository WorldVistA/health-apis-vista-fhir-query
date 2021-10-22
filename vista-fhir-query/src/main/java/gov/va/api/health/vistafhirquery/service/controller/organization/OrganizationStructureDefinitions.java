package gov.va.api.health.vistafhirquery.service.controller.organization;

/** System and urn:oid values for Organizations. */
public interface OrganizationStructureDefinitions {

  String ALLOW_MULTIPLE_BEDSECTIONS =
      "http://va.gov/fhir/StructureDefinition/organization-allowMultipleBedsections";

  String AMBULATORY_SURGERY_REVENUE_CODE =
      "http://va.gov/fhir/StructureDefinition/organization-ambulatorySurgeryRevenueCode";

  String AMBULATORY_SURGERY_REVENUE_CODE_URN_OID = "urn:oid:2.16.840.1.113883.6.301.3";

  String ANOTHER_COMPANY_PROCESSES_APPEALS =
      "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesAppeals";

  String ANOTHER_COMPANY_PROCESSES_DENTAL_CLAIMS =
      "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesDentalClaims";

  String ANOTHER_COMPANY_PROCESSES_INPAT_CLAIMS =
      "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesInpatClaims";

  String ANOTHER_COMPANY_PROCESSES_INQUIRIES =
      "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesInquiries";

  String ANOTHER_COMPANY_PROCESSES_OUTPAT_CLAIMS =
      "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesOutpatClaims";

  String ANOTHER_COMPANY_PROCESSES_PRECERT =
      "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesPrecert";

  String ANOTHER_COMPANY_PROCESSES_RX_CLAIMS =
      "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesRxClaims";

  String ATTENDING_RENDERING_PROVIDER_SECONDARY_IDINSTITUTIONAL_REQUIRED =
      "http://va.gov/fhir/StructureDefinition/organization-attendingRenderingProviderSecondaryIDInstitutionalRequired";

  String ATTENDING_RENDERING_PROVIDER_SECONDARY_IDPROFESIONAL_REQUIRED =
      "http://va.gov/fhir/StructureDefinition/organization-attendingRenderingProviderSecondaryIDProfesionalRequired";

  String BIN_NUMBER_CODE = "BIN";

  String EDI_ID_NUMBER_DENTAL_CODE = "DENTALEDI";

  String EDI_ID_NUMBER_PROF_CODE = "PROFEDI";

  String EDI_ID_NUMBER_INST_CODE = "INSTEDI";

  String EDI_INST_SECONDARY_ID_QUAL_1 = "urn:oid:2.16.840.1.113883.3.8901.3.1.36.68001";

  String EDI_INST_SECONDARY_ID_QUAL_2 = "urn:oid:2.16.840.1.113883.3.8901.3.1.36.68003";

  String EDI_PROF_SECONDARY_ID_QUAL_1 = "urn:oid:2.16.840.1.113883.3.8901.3.1.36.68005";

  String EDI_PROF_SECONDARY_ID_QUAL_2 = "urn:oid:2.16.840.1.113883.3.8901.3.1.36.68007";

  String ELECTRONIC_INSURANCE_TYPE =
      "http://va.gov/fhir/StructureDefinition/organization-electronicInsuranceType";

  String ELECTRONIC_INSURANCE_TYPE_URN_OID = "urn:oid:2.16.840.1.113883.3.8901.3.1.36.38009";

  String ELECTRONIC_TRANSMISSION_MODE =
      "http://va.gov/fhir/StructureDefinition/organization-electronicTransmissionMode";

  String ELECTRONIC_TRANSMISSION_MODE_URN_OID = "urn:oid:2.16.840.1.113883.3.8901.3.1.36.38001";

  String FILING_TIME_FRAME = "http://va.gov/fhir/StructureDefinition/organization-filingTimeFrame";

  String ONE_OUTPAT_VISIT_ON_BILL_ONLY =
      "http://va.gov/fhir/StructureDefinition/organization-oneOutpatVisitOnBillOnly";

  String PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500 =
      "http://va.gov/fhir/StructureDefinition/organization-performingProviderSecondIDTypeCMS1500";

  String PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001";

  String PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04 =
      "http://va.gov/fhir/StructureDefinition/organization-performingProviderSecondIDTypeUB04";

  String PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001";

  String PLAN_STANDARD_FILING_TIME_FRAME =
      "http://va.gov/fhir/StructureDefinition/organization-planStandardFilingTimeFrame";

  String PLAN_STANDARD_FILING_TIME_FRAME_URN_OID = "urn:oid:2.16.840.1.113883.3.8901.3.3558013";

  String PRESCRIPTION_REVENUE_CODE =
      "http://va.gov/fhir/StructureDefinition/organization-prescriptionRevenueCode";

  String PRESCRIPTION_REVENUE_CODE_URN_OID = "urn:oid:2.16.840.1.113883.6.301.3";

  String PRINT_SEC_MED_CLAIMS_WOMRALOCALLY =
      "http://va.gov/fhir/StructureDefinition/organization-printSecMedClaimsWOMRALocally";

  String PRINT_SEC_TERT_AUTO_CLAIMS_LOCALLY =
      "http://va.gov/fhir/StructureDefinition/organization-printSecTertAutoClaimsLocally";

  String REFERRNG_PROVIDER_SECOND_IDTYPE_CMS_1500 =
      "http://va.gov/fhir/StructureDefinition/organization-referrngProviderSecondIDTypeCMS1500";

  String REFERRNG_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001";

  String REFERRNG_PROVIDER_SECOND_IDTYPE_UB_04 =
      "http://va.gov/fhir/StructureDefinition/organization-referrngProviderSecondIDTypeUB04";

  String REFERRNG_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001";

  String SIGNATURE_REQUIRED_ON_BILL =
      "http://va.gov/fhir/StructureDefinition/organization-signatureRequiredOnBill";

  String TYPE_OF_COVERAGE = "http://va.gov/fhir/StructureDefinition/organization-typeOfCoverage";

  String TYPE_OF_COVERAGE_URN_OID = "urn:oid:2.16.840.1.113883.3.8901.3.36.8013";

  String VIA_INTERMEDIARY =
      "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary";

  String WILL_REIMBURSE_FOR_CARE =
      "http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare";

  String WILL_REIMBURSE_FOR_CARE_URN_OID = "urn:oid:2.16.840.1.113883.3.8901.3.1.36.1";
}
