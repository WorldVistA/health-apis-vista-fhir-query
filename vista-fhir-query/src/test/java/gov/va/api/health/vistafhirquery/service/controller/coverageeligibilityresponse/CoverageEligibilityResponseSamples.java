package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
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
      return coverageEligibilityResponse("p1", "8", "4");
    }

    public CoverageEligibilityResponse coverageEligibilityResponse(
        String patient, String limitation, String organization) {
      return CoverageEligibilityResponse.builder()
          .meta(Meta.builder().source("123").build())
          .id(
              PatientTypeCoordinates.builder()
                  .siteId("123")
                  .icn(patient)
                  .recordId(limitation)
                  .build()
                  .toString())
          .status(CoverageEligibilityResponse.Status.active)
          .purpose(List.of(CoverageEligibilityResponse.Purpose.discovery))
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
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    private Map<String, LhsLighthouseRpcGatewayResponse.Values> fields() {
      Map<String, LhsLighthouseRpcGatewayResponse.Values> fields = new HashMap<>();
      fields.put(
          PlanCoverageLimitations.PLAN,
          LhsLighthouseRpcGatewayResponse.Values.of("BCBS OF FL", "87"));
      return Map.copyOf(fields);
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults() {
      return getsManifestResults("8");
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file(PlanCoverageLimitations.FILE_NUMBER)
                      .ien(id)
                      .fields(fields())
                      .build()))
          .build();
    }
  }
}
