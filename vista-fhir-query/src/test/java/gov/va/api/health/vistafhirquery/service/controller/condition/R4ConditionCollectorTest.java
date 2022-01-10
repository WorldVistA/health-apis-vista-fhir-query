package gov.va.api.health.vistafhirquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class R4ConditionCollectorTest {

  @Test
  void labToFhir() {
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .results(ConditionProblemListSamples.Vista.create().results())
                .build()
                .toFhir())
        .containsExactly(ConditionProblemListSamples.R4.create().condition());
  }
}
