package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReference;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtectionAgent;
import java.util.stream.Stream;
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

  @Override
  public Stream<ProtectedReference> referencesOf(CoverageEligibilityResponse resource) {
    return Stream.of(
        protectedReferenceFactory.forResource(resource, resource::id),
        protectedReferenceFactory.forReferenceWithoutSite(resource.patient()).orElse(null),
        protectedReferenceFactory
            .forReference(resource.meta().source(), resource.insurer())
            .orElse(null));
  }
}
