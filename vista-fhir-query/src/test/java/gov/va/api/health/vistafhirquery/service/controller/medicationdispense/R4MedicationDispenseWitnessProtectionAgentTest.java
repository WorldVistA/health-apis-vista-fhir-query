package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import org.junit.jupiter.api.Test;

public class R4MedicationDispenseWitnessProtectionAgentTest {

  LinkProperties linkProperties = mock(LinkProperties.class);

  @Test
  void referencesOfT() {
    var medicationDispense =
        MedicationDispense.builder()
            .id("md1")
            .meta(Meta.builder().source("123").build())
            .subject(Reference.builder().reference("Patient/p1").build())
            .build();
    var wpa =
        new R4MedicationDispenseWitnessProtectionAgent(
            new ProtectedReferenceFactory(linkProperties));
    assertThat(
            wpa.referencesOf(medicationDispense)
                .map(pr -> pr.asResourceIdentity().orElse(null))
                .collect(toList()))
        .containsExactlyInAnyOrder(
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("MedicationDispense")
                .identifier("md1")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Patient")
                .identifier("p1")
                .build());
  }
}
