package gov.va.api.health.vistafhirquery.service.controller.coverage;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InsuranceBufferToR4CoverageTransformer {

  @NonNull String site;

  @NonNull LhsLighthouseRpcGatewayResponse.Results results;

  private Coverage toCoverage(LhsLighthouseRpcGatewayResponse.FilemanEntry result) {
    if (result == null) {
      return null;
    }
    return Coverage.builder().status(Status.draft).build();
  }

  public Stream<Coverage> toFhir() {
    return Safe.stream(results.results()).map(this::toCoverage).filter(Objects::nonNull);
  }
}
