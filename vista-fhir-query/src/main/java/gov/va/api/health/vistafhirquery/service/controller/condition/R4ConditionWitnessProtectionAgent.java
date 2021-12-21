package gov.va.api.health.vistafhirquery.service.controller.condition;

import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReference;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtectionAgent;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Creates protected references for the Condition resource. */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class R4ConditionWitnessProtectionAgent implements WitnessProtectionAgent<Condition> {
  @NonNull private final ProtectedReferenceFactory protectedReferenceFactory;

  @Override
  public Stream<ProtectedReference> referencesOf(Condition resource) {
    return Stream.concat(
        Stream.of(protectedReferenceFactory.forResource(resource, resource::id)),
        Stream.of(
            protectedReferenceFactory.forReferenceWithoutSite(resource.subject()).orElse(null)));
  }
}
