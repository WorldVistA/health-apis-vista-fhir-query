package gov.va.api.health.vistafhirquery.service.controller.appointment;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReference;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtectionAgent;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Creates protected references for the Appointment resource. */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class R4AppointmentWitnessProtectionAgent implements WitnessProtectionAgent<Appointment> {
  @NonNull private final ProtectedReferenceFactory protectedReferenceFactory;

  @Override
  public Stream<ProtectedReference> referencesOf(Appointment resource) {
    return Stream.concat(
        Stream.of(protectedReferenceFactory.forResource(resource, resource::id)),
        Safe.stream(resource.participant())
            .map(p -> protectedReferenceFactory.forReferenceWithoutSite(p.actor()).orElse(null)));
  }
}
