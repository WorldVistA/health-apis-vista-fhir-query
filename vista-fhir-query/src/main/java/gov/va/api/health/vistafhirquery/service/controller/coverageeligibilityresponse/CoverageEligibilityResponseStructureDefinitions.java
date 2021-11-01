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
      "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse"
          + "-militaryEngagementPeriod";
}
