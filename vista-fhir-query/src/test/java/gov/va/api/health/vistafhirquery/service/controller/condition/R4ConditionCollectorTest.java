package gov.va.api.health.vistafhirquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Visits;
import java.util.List;
import org.junit.jupiter.api.Test;

public class R4ConditionCollectorTest {
  @Test
  void clinicalStatusSearch() {
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .clinicalStatusCsv("active")
                .results(ConditionProblemListSamples.Vista.create().results())
                .build()
                .toFhir())
        .containsExactly(ConditionProblemListSamples.R4.create().condition());
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .clinicalStatusCsv("resolved,active")
                .results(ConditionProblemListSamples.Vista.create().results())
                .build()
                .toFhir())
        .containsExactly(ConditionProblemListSamples.R4.create().condition());
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .clinicalStatusCsv("active")
                .results(ConditionEncounterDiagnosisSamples.Vista.create().results())
                .build()
                .toFhir())
        .isEmpty();
  }

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
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .categoryCsv("problem-list-item,encounter-diagnosis")
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
  void problemOnlyToFhir() {
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .results(ConditionProblemListSamples.Vista.create().results())
                .build()
                .toFhir())
        .containsExactly(ConditionProblemListSamples.R4.create().condition());
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .categoryCsv("problem-list-item")
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
        .containsExactly(ConditionProblemListSamples.R4.create().condition());
  }

  @Test
  void visitOnlyToFhir() {
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .results(ConditionEncounterDiagnosisSamples.Vista.create().results())
                .build()
                .toFhir())
        .containsExactly(ConditionEncounterDiagnosisSamples.R4.create().condition());
    assertThat(
            R4ConditionCollector.builder()
                .patientIcn("p1")
                .site("123")
                .categoryCsv("encounter-diagnosis")
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
        .containsExactly(ConditionEncounterDiagnosisSamples.R4.create().condition());
  }
}
