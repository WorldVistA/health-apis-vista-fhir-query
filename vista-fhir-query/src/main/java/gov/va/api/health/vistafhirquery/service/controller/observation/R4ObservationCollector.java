package gov.va.api.health.vistafhirquery.service.controller.observation;

import static gov.va.api.health.vistafhirquery.service.controller.observation.VitalVuidMapper.forLoinc;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.vistafhirquery.service.util.CsvParameters;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Labs;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Vitals;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Collects Observations, Vitals before Labs. */
@Value
public class R4ObservationCollector {
  @NonNull String site;

  String patientIcn;

  VitalVuidMapper vitalVuidMapper;

  String codes;

  Set<String> categories;

  @NonNull VprGetPatientData.Response.Results results;

  @Builder
  R4ObservationCollector(
      String site,
      String patientIcn,
      VitalVuidMapper vitalVuidMapper,
      String codes,
      String categoryCsv,
      VprGetPatientData.Response.Results results,
      Map.Entry<String, VprGetPatientData.Response.Results> resultsEntry) {
    this.patientIcn = patientIcn;
    this.vitalVuidMapper = vitalVuidMapper;
    this.codes = codes;
    this.categories = CsvParameters.toSet(categoryCsv);
    // Backwards Compatibility
    // ToDo remove in task API-11858
    if (resultsEntry != null) {
      this.site = resultsEntry.getKey();
      this.results = resultsEntry.getValue();
    } else {
      this.site = site;
      this.results = results;
    }
  }

  private AllowedObservationCodes allowedCodes() {
    if (codes() == null) {
      return AllowedObservationCodes.allowAll();
    }
    List<String> loincCodes = CsvParameters.toList(codes());
    List<String> vuidCodes =
        loincCodes.stream()
            .flatMap(code -> vitalVuidMapper().mappings().stream().filter(forLoinc(code)))
            .map(VitalVuidMapper.VitalVuidMapping::vuid)
            .collect(toList());
    return AllowedObservationCodes.allowOnly(vuidCodes, loincCodes);
  }

  private Stream<Observation> labs() {
    return results()
        .labStream()
        .filter(Labs.Lab::isNotEmpty)
        .flatMap(
            lab ->
                VistaLabToR4ObservationTransformer.builder()
                    .patientIcn(patientIcn())
                    .vistaSiteId(site())
                    .vistaLab(lab)
                    .conditions(allowedCodes())
                    .build()
                    .conditionallyToFhir());
  }

  private Stream<Observation> observationsForCategory(String category) {
    switch (category) {
      case "laboratory":
        return labs();
      case "vital-signs":
        return vitals();
      default:
        return Stream.empty();
    }
  }

  Stream<Observation> toFhir() {
    if (categories().isEmpty()) {
      return Stream.concat(vitals(), labs());
    }
    return categories().stream().flatMap(this::observationsForCategory);
  }

  private Stream<Observation> vitals() {
    return results()
        .vitalStream()
        .filter(Vitals.Vital::isNotEmpty)
        .flatMap(
            vital ->
                VistaVitalToR4ObservationTransformer.builder()
                    .patientIcn(patientIcn())
                    .vistaSiteId(site())
                    .vuidMapper(vitalVuidMapper())
                    .vistaVital(vital)
                    .conditions(allowedCodes())
                    .build()
                    .conditionallyToFhir());
  }
}
