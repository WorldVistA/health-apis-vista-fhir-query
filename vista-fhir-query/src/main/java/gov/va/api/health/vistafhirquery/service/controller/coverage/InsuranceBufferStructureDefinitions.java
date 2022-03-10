package gov.va.api.health.vistafhirquery.service.controller.coverage;

public interface InsuranceBufferStructureDefinitions {
  String AMBULATORY_CARE_CERTIFICATION =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare";

  String BENEFITS_ASSIGNABLE =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable";

  String EXCLUDE_PREEXISTING_CONDITION =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions";

  String PRECERTIFICATION_REQUIRED =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired";

  String UTILIZATION_REVIEW_REQUIRED =
      "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired";
}
