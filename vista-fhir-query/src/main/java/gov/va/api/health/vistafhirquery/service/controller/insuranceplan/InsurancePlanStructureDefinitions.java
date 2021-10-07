package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

/** System and urn:oid values for InsurancePlan. */
public interface InsurancePlanStructureDefinitions {

  String AMBULATORY_CARE_CERTIFICATION =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare";

  String BANKING_IDENTIFICATION_NUMBER = "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68002";

  String BENEFITS_ASSIGNABLE =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable";

  String ELECTRONIC_PLAN_TYPE = "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8015";

  String EXCLUDE_PRE_EXISTING_CONDITION =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions";

  String GROUP_NUMBER = "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002";

  String IS_PRE_CERTIFICATION_REQUIRED =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired";

  String IS_UTILIZATION_REVIEW_REQUIRED =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired";

  String PLAN_CATEGORY = "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8014";

  String PLAN_ID = "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001";

  String PLAN_STANDARD_FTF =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame";

  String FILING_TIME_FRAME = "urn:oid:2.16.840.1.113883.3.8901.3.1.3558013";

  String PROCESSOR_CONTROL_NUMBER_PCN = "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68003";

  String TYPE_OF_PLAN = "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009";
}
