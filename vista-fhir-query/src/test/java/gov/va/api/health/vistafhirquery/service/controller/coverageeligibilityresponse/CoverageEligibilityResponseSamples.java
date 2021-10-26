package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
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
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberDates;
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
      var cer = coverageEligibilityResponse();
      cer.insurance().get(0).item().get(0).extension(itemExtensions());
      return cer;
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

    private Map<String, LhsLighthouseRpcGatewayResponse.Values> planLimitationFields() {
      Map<String, LhsLighthouseRpcGatewayResponse.Values> fields = new HashMap<>();
      fields.put(
          PlanCoverageLimitations.COVERAGE_CATEGORY,
          LhsLighthouseRpcGatewayResponse.Values.of("MENTAL HEALTH", "4"));
      fields.put(
          PlanCoverageLimitations.COVERAGE_STATUS,
          LhsLighthouseRpcGatewayResponse.Values.of("CONDITIONAL COVERAGE", "2"));
      fields.put(
          PlanCoverageLimitations.EFFECTIVE_DATE,
          LhsLighthouseRpcGatewayResponse.Values.of("JAN 12, 1992", "2920112"));
      fields.put(
          PlanCoverageLimitations.PLAN,
          LhsLighthouseRpcGatewayResponse.Values.of("BCBS OF FL", "87"));
      return Map.copyOf(fields);
    }

    private Map<String, LhsLighthouseRpcGatewayResponse.Values> subscriberDatesFields() {
      Map<String, LhsLighthouseRpcGatewayResponse.Values> fields = new HashMap<>();
      fields.put(
          SubscriberDates.DATE,
          LhsLighthouseRpcGatewayResponse.Values.of("MAY 20, 2018", "05-20-2018"));
      fields.put(
          SubscriberDates.DATE_QUALIFIER,
          LhsLighthouseRpcGatewayResponse.Values.of("PLAN BEGIN", "346"));
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
