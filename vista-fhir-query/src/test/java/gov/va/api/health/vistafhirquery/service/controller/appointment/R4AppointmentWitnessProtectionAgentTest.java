package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class R4AppointmentWitnessProtectionAgentTest {
  LinkProperties linkProperties = mock(LinkProperties.class);

  MockHttpServletRequest request = new MockHttpServletRequest();

  @Test
  void referencesOfT() {
    var appointment =
        Appointment.builder()
            .id("a1")
            .meta(Meta.builder().source("123").build())
            .participant(
                Appointment.Participant.builder()
                    .actor(Reference.builder().reference("Patient/p1").build())
                    .build()
                    .asList())
            .build();
    var wpa =
        new R4AppointmentWitnessProtectionAgent(new ProtectedReferenceFactory(linkProperties));
    assertThat(
            wpa.referencesOf(appointment)
                .map(pr -> pr.asResourceIdentity().orElse(null))
                .collect(toList()))
        .containsExactlyInAnyOrder(
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Appointment")
                .identifier("a1")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Patient")
                .identifier("p1")
                .build());
  }
}
