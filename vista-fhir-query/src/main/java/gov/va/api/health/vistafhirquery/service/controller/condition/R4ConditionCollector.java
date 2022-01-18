package gov.va.api.health.vistafhirquery.service.controller.condition;

import static java.util.stream.Collectors.toSet;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Problems;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Visits;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Collects Conditions, problems and visits. */
@Value
public class R4ConditionCollector {
  String patientIcn;

  Set<String> clinicalStatuses;

  @NonNull VprGetPatientData.Response.Results results;

  @NonNull String site;

  Set<String> categories;

  @Builder
  R4ConditionCollector(
      String site,
      String patientIcn,
      String clinicalStatusCsv,
      String categoryCsv,
      VprGetPatientData.Response.Results results) {
    this.site = site;
    this.patientIcn = patientIcn;
    this.categories =
        categoryCsv == null ? null : Arrays.stream(categoryCsv.split(",", -1)).collect(toSet());
    this.clinicalStatuses =
        clinicalStatusCsv == null
            ? null
            : Arrays.stream(clinicalStatusCsv.split(",", -1)).collect(toSet());
    this.results = results;
  }

  private boolean conditionHasRequestedClinicalStatus(Condition condition) {
    if (clinicalStatuses() == null) {
      return true;
    }
    if (condition.clinicalStatus() == null) {
      return false;
    }
    return Safe.stream(condition.clinicalStatus().coding())
        .anyMatch(coding -> clinicalStatuses().contains(coding.code()));
  }

  private Stream<Condition> conditionsForCategory(@NonNull String category) {
    switch (category) {
      case "problem-list-item":
        return problems();
      case "encounter-diagnosis":
        return visits();
      default:
        return Stream.empty();
    }
  }

  private Stream<Condition> problems() {
    return results
        .problemStream()
        .filter(Problems.Problem::isNotEmpty)
        .flatMap(
            problem ->
                VistaProblemToR4ConditionTransformer.builder()
                    .patientIcn(patientIcn)
                    .site(site)
                    .vistaProblem(problem)
                    .build()
                    .toFhir());
  }

  Stream<Condition> toFhir() {
    Stream<Condition> results;
    if (categories() == null) {
      results = Stream.concat(problems(), visits());
    } else {
      results = categories().stream().flatMap(this::conditionsForCategory);
    }
    return results.filter(this::conditionHasRequestedClinicalStatus);
  }

  private Stream<Condition> visits() {
    return results
        .visitsStream()
        .filter(Visits.Visit::isNotEmpty)
        .flatMap(
            visit ->
                VprVisitToR4ConditionTransformer.builder()
                    .patientIcn(patientIcn)
                    .site(site)
                    .vistaVisit(visit)
                    .build()
                    .toFhir());
  }
}
