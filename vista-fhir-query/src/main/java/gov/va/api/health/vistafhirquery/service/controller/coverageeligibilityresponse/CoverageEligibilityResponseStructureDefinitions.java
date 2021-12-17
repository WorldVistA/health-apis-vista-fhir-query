package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

public interface CoverageEligibilityResponseStructureDefinitions {
  String REQUESTED_SERVICE_TYPE =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-requestedServiceType";
  String REQUESTED_SERVICE_TYPE_SYSTEM = "2.16.840.1.113883.3.8901.3.1.280312.888002";
  String ELIGIBILITY_BENEFIT_INFO = "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8002";

  String ITEM_MODIFIER = "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.18003";

  String X12_YES_NO_SYSTEM = "urn:oid:2.16.840.1.113883.3.8901.3.1.36580033.8001";

  String ITEM_PRODUCT_OR_SERVICE = "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.18002";

  String ITEM_TERM = "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8007";

  String ITEM_UNIT = "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8003";

  String PLAN_LIMITATION_CATEGORY = "urn:oid:2.16.840.1.113883.3.8901.3.1.3558002.8002";

  String SUBSCRIBER_DATE =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDate";

  String SUBSCRIBER_DATE_PERIOD =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDatePeriod";

  String SUBSCRIBER_DATE_KIND =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDateKind";

  String SUBSCRIBER_DATE_QUALIFIER = "urn:oid:2.16.840.1.113883.3.8901.3.1.36580288.8003";

  String SERVICE_TYPES = "urn:oid:2.16.840.1.113883.3.8901.3.1.36580292.8001";

  String MILITARY_INFO_STATUS_CODE = "urn:oid:2.16.840.1.113883.3.8901.3.1.365.128001";
  String MILITARY_EMPLOYMENT_STATUS = "urn:oid:2.16.840.1.113883.3.8901.3.1.365.128002";
  String MILITARY_GOVT_AFFILIATION_CODE = "urn:oid:2.16.840.1.113883.3.8901.3.1.365.128003";
  String MILITARY_SERVICE_RANK_CODE = "urn:oid:2.16.840.1.113883.3.8901.3.1.365.128005";

  String MILITARY_INFO_STATUS_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition" + "/coverageEligibilityResponse-militaryInfoStatus";
  String MILITARY_EMPLOYMENT_STATUS_DEFINITION =
      "http://va.gov/fhir/StructureDefinition"
          + "/coverageEligibilityResponse-militaryEmploymentStatus";
  String MILITARY_GOVT_AFFILIATION_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition" + "/coverageEligibilityResponse-militaryAffiliation";
  String MILITARY_PERSONNEL_DESCRIPTION_DEFINITION =
      "http://va.gov/fhir/StructureDefinition"
          + "/coverageEligibilityResponse-militaryPersonnelDescription";
  String MILITARY_SERVICE_RANK_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition" + "/coverageEligibilityResponse-militaryRank";
  String DATE_TIME_PERIOD =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryEngagementPeriod";
  // Health Care Code Information Sub-File (#365.01)
  String HEALTH_CARE_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-healthCareCode";
  // Subscriber Reference Id Sub-File (#365.291)
  String SUBSCRIBER_REFERENCE_ID_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceId";
  String SUBSCRIBER_REFERENCE_ID_VALUE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdValue";
  String SUBSCRIBER_REFERENCE_ID_QUALIFIER_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdQualifier";
  String SUBSCRIBER_REFERENCE_ID_QUALIFIER = "2.16.840.1.113883.3.8901.3.1.36580291.8003";
  String SUBSCRIBER_REFERENCE_ID_DESCRIPTION_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdDescription";
  // Subscriber Additional Info Sub-File (#365.29)
  String SUBSCRIBER_ADDITIONAL_INFO_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberAdditionalInfo";
  String SUBSCRIBER_PLACE_OF_SERVICE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberPlaceOfService";
  String SUBSCRIBER_PLACE_OF_SERVICE_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658029.8002";
  String SUBSCRIBER_QUALIFIER_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberQualifier";
  String SUBSCRIBER_QUALIFIER_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658029.8004";
  String SUBSCRIBER_INJURY_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryCode";
  String SUBSCRIBER_INJURY_CODE_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658029.8005";
  String SUBSCRIBER_INJURY_CATEGORY_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryCategory";
  String SUBSCRIBER_INJURY_CATEGORY_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658029.8006";
  String SUBSCRIBER_INJURY_TEXT_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryText";
  // Healthcare Services Delivery (#365.27)
  String HEALTHCARE_SERVICES_DELIVERY =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-healthcareServicesDelivery";
  String BENEFIT_QUANTITY =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-benefitQuantity";
  String BENEFIT_PERCENT =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-benefitPercent";
  String BENEFIT_QUANTITY_CODE =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-benefitQuantityCode";
  String BENEFIT_QUANTITY_CODE_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658002.801";
  String QUANTITY_QUALIFIER =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-quantityQualifier";
  String QUANTITY_QUALIFIER_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658027.8003";
  String SAMPLE_SELECTION_MODULUS =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-sampleSelectionModulus";
  String UNITS_OF_MEASUREMENT =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-unitsOfMeasurement";
  String UNITS_OF_MEASUREMENT_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658027.8005";
  String TIME_PERIODS =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-timePeriods";
  String TIME_PERIOD_QUALIFIER =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-timePeriodQualifier";
  String TIME_PERIOD_QUALIFIER_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658027.8007";
  String DELIVERY_FREQUENCY =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-deliveryFrequency";
  String DELIVERY_FREQUENCY_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658027.8008";
  String DELIVERY_PATTERN =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-deliveryPattern";
  String DELIVERY_PATTERN_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3658027.8009";
  // Plan Coverage Limitations (355.32)
  String COVERAGE_CATEGORY =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-coverageCategory";
  String EFFECTIVE_DATE =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-effectiveDate";
  String COVERAGE_CATEGORY_SYSTEM = "2.16.840.1.113883.3.8901.3.1.3558032.8003";
}
