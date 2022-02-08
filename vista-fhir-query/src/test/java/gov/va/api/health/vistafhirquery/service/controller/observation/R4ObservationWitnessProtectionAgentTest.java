package gov.va.api.health.vistafhirquery.service.controller.observation;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import org.junit.jupiter.api.Test;

class R4ObservationWitnessProtectionAgentTest {
  LinkProperties linkProperties = mock(LinkProperties.class);

  @Test
  void referencesOfT() {
    var observation =
        Observation.builder()
            .id("o1")
            .subject(Reference.builder().reference("Patient/123").build())
            .build();
    var wpa =
        new R4ObservationWitnessProtectionAgent(new ProtectedReferenceFactory(linkProperties));
    assertThat(
            wpa.referencesOf(observation)
                .map(pr -> pr.asResourceIdentity().orElse(null))
                .collect(toList()))
        .containsExactlyInAnyOrder(
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Observation")
                .identifier("o1")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Patient")
                .identifier("123")
                .build());
  }
}
