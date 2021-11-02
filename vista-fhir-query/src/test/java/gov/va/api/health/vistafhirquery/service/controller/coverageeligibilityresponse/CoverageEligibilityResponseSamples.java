package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Money;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Insurance;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Item;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Purpose;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.EligibilityBenefit;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.IivResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Values;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.ServiceTypes;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberDates;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CoverageEligibilityResponseSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    public static CoverageEligibilityResponse.Bundle asBundle(
        String baseUrl,
        Collection<CoverageEligibilityResponse> resources,
        int totalRecords,
        BundleLink... links) {
      return CoverageEligibilityResponse.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      resource ->
                          CoverageEligibilityResponse.Entry.builder()
                              .fullUrl(baseUrl + "/CoverageEligibilityResponse/" + resource.id())
                              .resource(resource)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static BundleLink link(BundleLink.LinkRelation rel, String base, String query) {
      return BundleLink.builder().relation(rel).url(base + "?" + query).build();
    }

    private List<CoverageEligibilityResponse.Benefit> benefits() {
      return CoverageEligibilityResponse.Benefit.builder()
          .type(
              CodeableConcept.builder()
                  .coding(
                      Coding.builder()
                          .code("1")
                          .system(
                              CoverageEligibilityResponseStructureDefinitions
                                  .ELIGIBILITY_BENEFIT_INFO)
                          .build()
                          .asList())
                  .build())
          .allowedMoney(Money.builder().value(new BigDecimal("250.25")).build())
          .build()
          .asList();
    }

    private CodeableConcept codeableConceptFor(String system, String code) {
      return CodeableConcept.builder()
          .coding(Coding.builder().system(system).code(code).build().asList())
          .build();
    }

    public CoverageEligibilityResponse coverageEligibilityResponse() {
      return coverageEligibilityResponse("p1", "8", "4", "1,8,");
    }

    public CoverageEligibilityResponse coverageEligibilityResponse(
        String patient, String limitation, String organization, String coverage) {
      return CoverageEligibilityResponse.builder()
          .meta(Meta.builder().source("123").build())
          .id(
              PatientTypeCoordinates.builder()
                  .site("123")
                  .icn(patient)
                  .ien(coverage)
                  .build()
                  .toString())
          .status(CoverageEligibilityResponse.Status.active)
          .purpose(List.of(Purpose.benefits, Purpose.discovery))
          .patient(Reference.builder().reference("Patient/" + patient).build())
          .created("ignored")
          ._request(DataAbsentReason.of(DataAbsentReason.Reason.unsupported))
          .outcome(CoverageEligibilityResponse.Outcome.complete)
          .insurer(
              Reference.builder()
                  .reference(
                      "Organization/"
                          + RecordCoordinates.builder()
                              .site("123")
                              .file(InsuranceCompany.FILE_NUMBER)
                              .ien(organization)
                              .build()
                              .toString())
                  .build())
          .insurance(List.of(insurance(patient, coverage)))
          .build();
    }

    public CoverageEligibilityResponse coverageEligibilityResponseForWrite() {
      return coverageEligibilityResponseForWrite("123", "p1", "1,8,");
    }

    public CoverageEligibilityResponse coverageEligibilityResponseForWrite(
        String site, String patient, String coverage) {
      return CoverageEligibilityResponse.builder()
          .meta(Meta.builder().source("123").build())
          .id(
              PatientTypeCoordinates.builder()
                  .site(site)
                  .icn(patient)
                  .ien(coverage)
                  .build()
                  .toString())
          .identifier(
              List.of(
                  Identifier.builder()
                      .type(CodeableConcept.builder().text("MSH-10").build())
                      .value("MCI-1234")
                      .build(),
                  Identifier.builder()
                      .type(CodeableConcept.builder().text("MSA-3").build())
                      .value("TN-1234")
                      .build()))
          .insurer(
              Reference.builder()
                  .reference(
                      "Organization/"
                          + RecordCoordinates.builder()
                              .file(Payer.FILE_NUMBER)
                              .site(site)
                              .ien("8")
                              .build())
                  .build())
          .extension(extensions())
          .servicedDate("2010-01-20")
          .insurance(
              Insurance.builder()
                  .benefitPeriod(Period.builder().start("2004-02-03").end("2007-05-06").build())
                  .item(
                      Item.builder()
                          .extension(itemExtensions())
                          .category(
                              codeableConceptFor(
                                  CoverageEligibilityResponseStructureDefinitions.SERVICE_TYPES,
                                  "OTHER MEDICAL"))
                          .benefit(benefits())
                          .modifier(
                              codeableConceptFor(
                                      CoverageEligibilityResponseStructureDefinitions.ITEM_MODIFIER,
                                      "Modified")
                                  .asList())
                          .term(
                              codeableConceptFor(
                                  CoverageEligibilityResponseStructureDefinitions.ITEM_TERM, "7"))
                          .unit(
                              codeableConceptFor(
                                  CoverageEligibilityResponseStructureDefinitions.ITEM_UNIT, "2"))
                          .productOrService(codeableConceptFor("N4", "IE123"))
                          .network(
                              codeableConceptFor(
                                  CoverageEligibilityResponseStructureDefinitions.X12_YES_NO_SYSTEM,
                                  "N"))
                          .authorizationRequired(true)
                          .build()
                          .asList())
                  .build()
                  .asList())
          .build();
    }

    private List<Extension> extensions() {
      return List.of(
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_INFO_STATUS_CODE_DEFINITION)
              .valueCodeableConcept(
                  codeableConceptFor(
                      CoverageEligibilityResponseStructureDefinitions.MILITARY_INFO_STATUS_CODE,
                      "P"))
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_EMPLOYMENT_STATUS_DEFINITION)
              .valueCodeableConcept(
                  codeableConceptFor(
                      CoverageEligibilityResponseStructureDefinitions.MILITARY_EMPLOYMENT_STATUS,
                      "CC"))
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_GOVT_AFFILIATION_CODE_DEFINITION)
              .valueCodeableConcept(
                  codeableConceptFor(
                      CoverageEligibilityResponseStructureDefinitions
                          .MILITARY_GOVT_AFFILIATION_CODE,
                      "C"))
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_PERSONNEL_DESCRIPTION_DEFINITION)
              .valueString("ARMY")
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_SERVICE_RANK_CODE_DEFINITION)
              .valueCodeableConcept(
                  codeableConceptFor(
                      CoverageEligibilityResponseStructureDefinitions.MILITARY_SERVICE_RANK_CODE,
                      "C1"))
              .build());
    }

    private Insurance insurance(String patient, String coverage) {
      return Insurance.builder()
          .coverage(
              Reference.builder()
                  .reference(
                      "Coverage/"
                          + PatientTypeCoordinates.builder()
                              .site("123")
                              .icn(patient)
                              .ien(coverage)
                              .build()
                              .toString())
                  .build())
          .inforce(true)
          .benefitPeriod(Period.builder().start("1992-01-12T00:00:00Z").build())
          .item(
              List.of(
                  Item.builder()
                      .category(
                          CodeableConcept.builder()
                              .coding(
                                  List.of(
                                      Coding.builder()
                                          .system(
                                              "urn:oid:2.16.840.1.113883.3.8901.3.1.3558002.8002")
                                          .code("MENTAL HEALTH")
                                          .build()))
                              .build())
                      .excluded(true)
                      .build()))
          .build();
    }

    private List<Extension> itemExtensions() {
      return List.of(
          Extension.builder()
              .url(CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE)
              .extension(
                  List.of(
                      Extension.builder()
                          .url(
                              CoverageEligibilityResponseStructureDefinitions
                                  .SUBSCRIBER_DATE_PERIOD)
                          .valuePeriod(Period.builder().start("2018-05-20").build())
                          .build(),
                      Extension.builder()
                          .url(CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE_KIND)
                          .valueCodeableConcept(
                              CodeableConcept.builder()
                                  .coding(
                                      List.of(
                                          Coding.builder()
                                              .system(
                                                  CoverageEligibilityResponseStructureDefinitions
                                                      .SUBSCRIBER_DATE_QUALIFIER)
                                              .code("PLAN BEGIN")
                                              .build()))
                                  .build())
                          .build()))
              .build());
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    public List<WriteableFilemanValue> eligibilityBenefitFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.ELIGIBILITY_BENEFIT_INFO)
              .value("1")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.COVERAGE_LEVEL)
              .value("2")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.TIME_PERIOD_QUALIFIER)
              .value("7")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.MONETARY_AMOUNT)
              .value("250.25")
              .build(), /*          WriteableFilemanValue.builder()
                            .file(EligibilityBenefit.FILE_NUMBER)
                            .index(1)
                            .field(EligibilityBenefit.PERCENT)
                            .value("88.88")
                            .build(),
                        WriteableFilemanValue.builder()
                            .file(EligibilityBenefit.FILE_NUMBER)
                            .index(1)
                            .field(EligibilityBenefit.QUANTITY_QUALIFIER)
                            // M2 Maximum
                            .value("M2")
                            .build(),
                        WriteableFilemanValue.builder()
                            .file(EligibilityBenefit.FILE_NUMBER)
                            .index(1)
                            .field(EligibilityBenefit.QUANTITY)
                            .value("666")
                            .build(),*/
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.AUTHORIZATION_CERTIFICATION)
              .value("Y")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.IN_PLAN)
              .value("N")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.PROCEDURE_CODING_METHOD)
              .value("N4")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.PROCEDURE_CODE)
              .value("IE123")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.PROCEDURE_MODIFIER_1)
              .value("Modified")
              .build());
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults() {
      return getsManifestResults("8");
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  FilemanEntry.builder()
                      .file(PlanCoverageLimitations.FILE_NUMBER)
                      .ien(id)
                      .fields(planLimitationFields())
                      .build(),
                  FilemanEntry.builder()
                      .file(SubscriberDates.FILE_NUMBER)
                      .ien(id)
                      .fields(subscriberDatesFields())
                      .build()))
          .build();
    }

    public List<WriteableFilemanValue> ienMacroPointers() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365^1^IEN}")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberDates.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365.02^1^IEN}")
              .build(),
          WriteableFilemanValue.builder()
              .file(ServiceTypes.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365.02^1^IEN}")
              .build());
    }

    public List<WriteableFilemanValue> iivResponseFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MESSAGE_CONTROL_ID)
              .value("MCI-1234")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.PAYER)
              .value("8")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.TRACE_NUMBER)
              .value("TN-1234")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.SERVICE_DATE)
              .value("01-20-2010")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_INFO_STATUS_CODE)
              .value("P")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_EMPLOYMENT_STATUS)
              .value("CC")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_GOVT_AFFILIATION_CODE)
              .value("C")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_PERSONNEL_DESCRIPTION)
              .value("ARMY")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_SERVICE_RANK_CODE)
              .value("C1")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.DATE_TIME_PERIOD)
              .value("02032004-05062007")
              .build());
    }

    private Map<String, Values> planLimitationFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(PlanCoverageLimitations.COVERAGE_CATEGORY, Values.of("MENTAL HEALTH", "4"));
      fields.put(PlanCoverageLimitations.COVERAGE_STATUS, Values.of("CONDITIONAL COVERAGE", "2"));
      fields.put(PlanCoverageLimitations.EFFECTIVE_DATE, Values.of("JAN 12, 1992", "2920112"));
      fields.put(PlanCoverageLimitations.PLAN, Values.of("BCBS OF FL", "87"));
      return Map.copyOf(fields);
    }

    public List<WriteableFilemanValue> serviceTypesFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(ServiceTypes.FILE_NUMBER)
              .index(1)
              .field(ServiceTypes.SERVICE_TYPES)
              .value("OTHER MEDICAL")
              .build());
    }

    private Map<String, Values> subscriberDatesFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(SubscriberDates.DATE, Values.of("MAY 20, 2018", "05-20-2018"));
      fields.put(SubscriberDates.DATE_QUALIFIER, Values.of("PLAN BEGIN", "346"));
      return Map.copyOf(fields);
    }

    public List<WriteableFilemanValue> subscriberDatesFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(SubscriberDates.FILE_NUMBER)
              .index(1)
              .field(SubscriberDates.DATE)
              .value("05-20-2018")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberDates.FILE_NUMBER)
              .index(1)
              .field(SubscriberDates.DATE_QUALIFIER)
              .value("PLAN BEGIN")
              .build());
    }
  }
}
