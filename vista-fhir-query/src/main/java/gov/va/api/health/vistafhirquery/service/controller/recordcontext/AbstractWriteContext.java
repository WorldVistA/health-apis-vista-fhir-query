package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifySiteSpecificVistaResponseOrDie;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.charonclient.LhsGatewayErrorHandler;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractWriteContext<BodyT extends IsResource>
    implements WriteContext<BodyT> {

  @Getter private final @NonNull String fileNumber;
  @Getter private final @NonNull String site;
  @Getter private final @NonNull BodyT body;
  @Getter private LhsLighthouseRpcGatewayResponse.FilemanEntry result;

  /** Validate and set the result. */
  public void result(LhsLighthouseRpcGatewayResponse response) {
    verifySiteSpecificVistaResponseOrDie(site(), response);
    var resultsForStation = response.resultsByStation().get(site());
    LhsGatewayErrorHandler.of(resultsForStation).validateResults();
    var insTypeResults =
        resultsForStation.results().stream()
            .filter(entry -> fileNumber().equals(entry.file()))
            .toList();
    if (insTypeResults.size() != 1) {
      throw ExpectationFailed.because("Unexpected number of results: " + insTypeResults.size());
    }
    this.result = insTypeResults.get(0);
  }
}
