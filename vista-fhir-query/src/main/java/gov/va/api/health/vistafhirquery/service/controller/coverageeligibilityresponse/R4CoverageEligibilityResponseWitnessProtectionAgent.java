package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.fhir.api.Safe.stream;

import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReference;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.RequestPayloadModifier;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtectionAgent;
import java.util.Objects;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Creates protected references for the CoverageEligibilityResponse resource. */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
public class R4CoverageEligibilityResponseWitnessProtectionAgent
    implements WitnessProtectionAgent<CoverageEligibilityResponse> {
  private final ProtectedReferenceFactory protectedReferenceFactory;

  private final HttpServletRequest request;

  @Override
  public Stream<ProtectedReference> referencesOf(CoverageEligibilityResponse resource) {
    RequestPayloadModifier.forPayload(resource)
        .request(request)
        .addMeta(resource::meta)
        .build()
        .applyModifications();
    return Stream.concat(
        Stream.of(
                protectedReferenceFactory.forResource(resource, resource::id),
                protectedReferenceFactory.forReferenceWithoutSite(resource.patient()).orElse(null),
                protectedReferenceFactory
                    .forReference(resource.meta().source(), resource.insurer())
                    .orElse(null))
            .filter(Objects::nonNull),
        stream(resource.insurance())
            .map(
                ins ->
                    protectedReferenceFactory
                        .forReference(resource.meta().source(), ins.coverage())
                        .orElse(null))
            .filter(Objects::nonNull));
  }
}
