package gov.va.api.health.vistafhirquery.service.controller.coverage;

public interface CoverageStructureDefinitions {
  String COVERAGE_CLASS_CODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/coverage-class";
  String STOP_POLICY_FROM_BILLING =
      "http://va.gov/fhir/StructureDefinition/coverage-stopPolicyFromBilling";
  String SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/subscriber-relationship";
}
