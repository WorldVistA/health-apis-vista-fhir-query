package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

public class CoverageEligibilityResponseStructureDefinitions {
  public static final String ELIGIBILITY_BENEFIT_INFO =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8002";

  public static final String ITEM_MODIFIER = "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.18003";

  public static final String X12_YES_NO_SYSTEM =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.36580033.8001";

  public static final String ITEM_PRODUCT_OR_SERVICE =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.18002";

  public static final String ITEM_TERM = "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8007";

  public static final String ITEM_UNIT = "urn:oid:2.16.840.1.113883.3.8901.3.1.3658002.8003";

  public static final String PLAN_LIMITATION_CATEGORY =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.3558002.8002";

  public static final String SUBSCRIBER_DATE =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDate";

  public static final String SUBSCRIBER_DATE_PERIOD =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDatePeriod";

  public static final String SUBSCRIBER_DATE_KIND =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberDateKind";

  public static final String SUBSCRIBER_DATE_QUALIFIER =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.36580288.8003";

  public static final String SERVICE_TYPES = "urn:oid:2.16.840.1.113883.3.8901.3.1.36580292.8001";

  public static final String MILITARY_INFO_STATUS_CODE =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.365.128001";
  public static final String MILITARY_EMPLOYMENT_STATUS =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.365.128002";
  public static final String MILITARY_GOVT_AFFILIATION_CODE =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.365.128003";
  public static final String MILITARY_SERVICE_RANK_CODE =
      "urn:oid:2.16.840.1.113883.3.8901.3.1.365.128005";

  public static final String MILITARY_INFO_STATUS_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition" + "/coverageEligibilityResponse-militaryInfoStatus";
  public static final String MILITARY_EMPLOYMENT_STATUS_DEFINITION =
      "http://va.gov/fhir/StructureDefinition"
          + "/coverageEligibilityResponse-militaryEmploymentStatus";
  public static final String MILITARY_GOVT_AFFILIATION_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition" + "/coverageEligibilityResponse-militaryAffiliation";
  public static final String MILITARY_PERSONNEL_DESCRIPTION_DEFINITION =
      "http://va.gov/fhir/StructureDefinition"
          + "/coverageEligibilityResponse-militaryPersonnelDescription";
  public static final String MILITARY_SERVICE_RANK_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition" + "/coverageEligibilityResponse-militaryRank";
  public static final String DATE_TIME_PERIOD =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-militaryEngagementPeriod";
  // Health Care Code Information Sub-File (#365.01)
  public static final String HEALTH_CARE_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-healthCareCode";
  // Subscriber Reference Id Sub-File (#365.291)
  public static final String SUBSCRIBER_REFERENCE_ID_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceId";
  public static final String SUBSCRIBER_REFERENCE_ID_VALUE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdValue";
  public static final String SUBSCRIBER_REFERENCE_ID_QUALIFIER_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdQualifier";
  public static final String SUBSCRIBER_REFERENCE_ID_QUALIFIER =
      "2.16.840.1.113883.3.8901.3.1.36580291.8003";
  public static final String SUBSCRIBER_REFERENCE_ID_DESCRIPTION_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberReferenceIdDescription";
  // Subscriber Additional Info Sub-File (#365.29)
  public static final String SUBSCRIBER_ADDITIONAL_INFO_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberAdditionalInfo";
  public static final String SUBSCRIBER_PLACE_OF_SERVICE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberPlaceOfService";
  public static final String SUBSCRIBER_PLACE_OF_SERVICE_SYSTEM =
      "2.16.840.1.113883.3.8901.3.1.3658029.8002";
  public static final String SUBSCRIBER_QUALIFIER_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberQualifier";
  public static final String SUBSCRIBER_QUALIFIER_SYSTEM =
      "2.16.840.1.113883.3.8901.3.1.3658029.8004";
  public static final String SUBSCRIBER_INJURY_CODE_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryCode";
  public static final String SUBSCRIBER_INJURY_CODE_SYSTEM =
      "2.16.840.1.113883.3.8901.3.1.3658029.8005";
  public static final String SUBSCRIBER_INJURY_CATEGORY_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryCategory";
  public static final String SUBSCRIBER_INJURY_CATEGORY_SYSTEM =
      "2.16.840.1.113883.3.8901.3.1.3658029.8006";
  public static final String SUBSCRIBER_INJURY_TEXT_DEFINITION =
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-subscriberInjuryText";
}
