package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import org.junit.jupiter.api.Test;

public class R4MedicationRequestWitnessProtectionAgentTest {

  LinkProperties linkProperties = mock(LinkProperties.class);

  @Test
  void referencesOfT() {
    MedicationRequest medicationRequest =
        MedicationRequest.builder()
            .id("mr1")
            .meta(Meta.builder().source("123").build())
            .subject(toReference("Patient", "p1", null))
            .build();
    R4MedicationRequestWitnessProtectionAgent wpa =
        new R4MedicationRequestWitnessProtectionAgent(
            new ProtectedReferenceFactory(linkProperties));
    assertThat(
            wpa.referencesOf(medicationRequest)
                .map(pr -> pr.asResourceIdentity().orElse(null))
                .collect(toList()))
        .containsExactlyInAnyOrder(
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("MedicationRequest")
                .identifier("mr1")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Patient")
                .identifier("p1")
                .build());
  }
}
