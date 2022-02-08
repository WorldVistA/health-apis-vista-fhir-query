package gov.va.api.health.vistafhirquery.service.controller.condition;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import org.junit.jupiter.api.Test;

class R4ConditionWitnessProtectionAgentTest {
  LinkProperties linkProperties = mock(LinkProperties.class);

  @Test
  void referencesOfT() {
    var condition =
        Condition.builder()
            .id("c1")
            .subject(Reference.builder().reference("Patient/123").build())
            .build();
    var wpa = new R4ConditionWitnessProtectionAgent(new ProtectedReferenceFactory(linkProperties));
    assertThat(
            wpa.referencesOf(condition)
                .map(pr -> pr.asResourceIdentity().orElse(null))
                .collect(toList()))
        .containsExactlyInAnyOrder(
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Condition")
                .identifier("c1")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Patient")
                .identifier("123")
                .build());
  }
}
