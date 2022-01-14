package gov.va.api.health.vistafhirquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Visits;
import java.util.List;
import org.junit.jupiter.api.Test;

public class R4ConditionCollectorTest {
  @Test
  void problemAndVisitToFhir() {
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .results(
                    ConditionProblemListSamples.Vista.create()
                        .results()
                        .visits(
                            Visits.builder()
                                .visitResults(
                                    (List.of(
                                        ConditionEncounterDiagnosisSamples.Vista.create().visit())))
                                .build()))
                .build()
                .toFhir())
        .contains(
            ConditionProblemListSamples.R4.create().condition(),
            ConditionEncounterDiagnosisSamples.R4.create().condition());
  }

  @Test
  void problemToFhir() {
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .results(ConditionProblemListSamples.Vista.create().results())
                .build()
                .toFhir())
        .containsExactly(ConditionProblemListSamples.R4.create().condition());
  }

  @Test
  void visitToFhir() {
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .results(ConditionEncounterDiagnosisSamples.Vista.create().results())
                .build()
                .toFhir())
        .containsExactly(ConditionEncounterDiagnosisSamples.R4.create().condition());
  }
}
