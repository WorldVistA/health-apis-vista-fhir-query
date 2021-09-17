package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReference;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtectionAgent;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Creates protected references for the InsurancePlan resource. */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
public class R4InsurancePlanWitnessProtectionAgent
    implements WitnessProtectionAgent<InsurancePlan> {
  private final ProtectedReferenceFactory protectedReferenceFactory;

  @Override
  public Stream<ProtectedReference> referencesOf(InsurancePlan resource) {
    return Stream.concat(
        Stream.of(protectedReferenceFactory.forResource(resource, resource::id)),
        Stream.of(
                protectedReferenceFactory
                    .forReference(resource.meta().source(), resource.ownedBy())
                    .orElse(null))
            .filter(Objects::nonNull));
  }
}
