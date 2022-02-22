package gov.va.api.health.vistafhirquery.service.controller.coverage;

public interface InsuranceBufferStructureDefinitions {

  String AMBULATORY_CARE_CERTIFICATION =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare";

  String BANKING_IDENTIFICATION_NUMBER = "urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.40801";

  String BENEFITS_ASSIGNABLE =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable";

  String EXCLUDE_PREEXISTING_CONDITION =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions";

  String GROUP_NUMBER = "urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.908002";

  String PRECERTIFICATION_REQUIRED =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired";

  String REIMBURSE = "http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare";

  String REIMBURSE_URN_OID = "urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.208005";

  String INQ_SERVICE_TYPE_CODE = "urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.8808001";

  String UTILIZATION_REVIEW_REQUIRED =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired";

  String PROCESSOR_CONTROL_NUMBER_PCN = "urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.408011";

  String TYPE_OF_PLAN = "urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.408009";
}
