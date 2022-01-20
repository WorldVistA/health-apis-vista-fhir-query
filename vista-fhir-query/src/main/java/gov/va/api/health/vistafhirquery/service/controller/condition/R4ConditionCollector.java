package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
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

  String code;

  DateSearchBoundaries onsetDate;

  @Builder
  R4ConditionCollector(
      String site,
      String patientIcn,
      String clinicalStatusCsv,
      String categoryCsv,
      String code,
      String[] date,
      VprGetPatientData.Response.Results results) {
    this.site = site;
    this.patientIcn = patientIcn;
    this.categories =
        isBlank(categoryCsv)
            ? emptySet()
            : Arrays.stream(categoryCsv.split(",", -1)).collect(toSet());
    this.clinicalStatuses =
        isBlank(clinicalStatusCsv)
            ? emptySet()
            : Arrays.stream(clinicalStatusCsv.split(",", -1)).collect(toSet());
    this.code = code;
    this.results = results;
    this.onsetDate = isBlank(date) ? null : DateSearchBoundaries.of(date);
  }

  private boolean conditionHasRequestedClinicalStatus(Condition condition) {
    if (isBlank(clinicalStatuses())) {
      return true;
    }
    if (isBlank(condition.clinicalStatus())) {
      return false;
    }
    return Safe.stream(condition.clinicalStatus().coding())
        .anyMatch(coding -> clinicalStatuses().contains(coding.code()));
  }

  private boolean conditionHasRequestedCode(Condition condition) {
    if (isBlank(code())) {
      return true;
    }
    return Safe.stream(condition.code().coding()).anyMatch(coding -> code().equals(coding.code()));
  }

  private boolean conditionHasRequestedOnsetDate(Condition condition) {
    if (isBlank(onsetDate())) {
      return true;
    }
    return onsetDate().isDateWithinBounds(condition.onsetDateTime());
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
    if (isBlank(categories())) {
      results = Stream.concat(problems(), visits());
    } else {
      results = categories().stream().flatMap(this::conditionsForCategory);
    }
    return results
        .filter(this::conditionHasRequestedClinicalStatus)
        .filter(this::conditionHasRequestedCode)
        .filter(this::conditionHasRequestedOnsetDate);
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
