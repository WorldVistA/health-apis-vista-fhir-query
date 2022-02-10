package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReference;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtectionAgent;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class R4MedicationDispenseWitnessProtectionAgent
    implements WitnessProtectionAgent<MedicationDispense> {

  @NonNull private final ProtectedReferenceFactory protectedReferenceFactory;

  @Override
  public Stream<ProtectedReference> referencesOf(MedicationDispense resource) {
    return Stream.of(
        protectedReferenceFactory.forResource(resource, resource::id),
        protectedReferenceFactory.forReferenceWithoutSite(resource.subject()).orElse(null));
  }
}