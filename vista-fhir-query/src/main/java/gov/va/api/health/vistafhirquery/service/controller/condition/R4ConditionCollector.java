package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.util.CsvParameters.toSetOrDefault;
import static gov.va.api.health.vistafhirquery.service.util.Predicates.noFilter;
import static gov.va.api.health.vistafhirquery.service.util.Predicates.selectAll;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
import gov.va.api.health.vistafhirquery.service.util.CsvParameters;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Problems;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Visits;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Collects Conditions, problems and visits. */
@Value
public class R4ConditionCollector {

  private static final String PROBLEM_LIST_ITEM = "problem-list-item";

  private static final String ENCOUNTER_DIAGNOSIS = "encounter-diagnosis";

  String patientIcn;

  @NonNull VprGetPatientData.Response.Results results;

  @NonNull String site;

  List<Supplier<Stream<Condition>>> conditionSuppliers;

  Predicate<Condition> conditionFilter;

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
    this.results = results;
    this.conditionSuppliers = suppliersForCategories(categoryCsv);
    this.conditionFilter =
        selectAll(Condition.class)
            .and(filterConditionsWithClinicalStatus(clinicalStatusCsv))
            .and(filterConditionsWithRequestedCode(code))
            .and(filterConditionWithOnsetDate(date));
  }

  private static Predicate<Condition> filterConditionWithOnsetDate(String[] onsetDate) {
    if (onsetDate == null || onsetDate.length == 0) {
      return noFilter();
    }
    var boundaries = DateSearchBoundaries.of(onsetDate);
    return condition -> boundaries.isDateWithinBounds(condition.onsetDateTime());
  }

  private static Predicate<Condition> filterConditionsWithClinicalStatus(String clinicalStatusCsv) {
    Set<String> clinicalStatuses = CsvParameters.toSet(clinicalStatusCsv);
    if (clinicalStatuses.isEmpty()) {
      return noFilter();
    }
    return condition ->
        !isBlank(condition.clinicalStatus())
            && Safe.stream(condition.clinicalStatus().coding())
                .anyMatch(coding -> clinicalStatuses.contains(coding.code()));
  }

  private static Predicate<Condition> filterConditionsWithRequestedCode(String code) {
    if (isBlank(code)) {
      return noFilter();
    }
    return condition ->
        Safe.stream(condition.code().coding()).anyMatch(coding -> code.equals(coding.code()));
  }

  private List<Supplier<Stream<Condition>>> suppliersForCategories(String categoryCsv) {
    var categories =
        toSetOrDefault(categoryCsv, () -> Set.of(PROBLEM_LIST_ITEM, ENCOUNTER_DIAGNOSIS));
    List<Supplier<Stream<Condition>>> suppliers = new ArrayList<>(2);
    if (categories.contains(PROBLEM_LIST_ITEM)) {
      suppliers.add(this::supplyFromProblems);
    }
    if (categories.contains(ENCOUNTER_DIAGNOSIS)) {
      suppliers.add(this::supplyFromVisits);
    }
    return suppliers;
  }

  private Stream<Condition> supplyFromProblems() {
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

  private Stream<Condition> supplyFromVisits() {
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

  Stream<Condition> toFhir() {
    return conditionSuppliers().stream().flatMap(Supplier::get).filter(conditionFilter());
  }
}
