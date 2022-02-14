package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ALL")
class R4MedicationDispenseTransformerTest {
  private Stream<MedicationDispense> _transform(VprGetPatientData.Response.Results results) {
    return R4MedicationDispenseTransformer.builder()
        .site("673")
        .patientIcn("p1")
        .rpcResults(results)
        .fillDateFilter(Optional.empty())
        .build()
        .toFhir();
  }

  @Test
  void medicationDispenseIsEmpty() {
    assertThat(_transform(VprGetPatientData.Response.Results.builder().build())).isEmpty();
  }

  @Test
  void toFhirProductMapsToMedicationCodeableConcept() {
    assertThat(
            _transform(MedicationDispenseSamples.Vista.create().resultsWithEmptyProduct())
                .findFirst()
                .get())
        .hasFieldOrPropertyWithValue("medicationCodeableConcept", null);
    assertThat(
            _transform(MedicationDispenseSamples.Vista.create().resultsWithNullProductClazz())
                .findFirst()
                .get())
        .hasFieldOrPropertyWithValue(
            "medicationCodeableConcept", CodeableConcept.builder().text("WARFARIN").build());
  }

  @Test
  void toFhirReleaseDateMapsToStatus() {
    assertThat(_transform(MedicationDispenseSamples.Vista.create().results()).findFirst().get())
        .isEqualTo(MedicationDispenseSamples.R4.create().medicationDispense());
    assertThat(
            _transform(
                    MedicationDispenseSamples.Vista.create()
                        .results(MedicationDispenseSamples.Vista.create().unreleasedMed()))
                .findFirst()
                .get())
        .isEqualTo(MedicationDispenseSamples.R4.create().medicationDispenseInProgress());
  }

  @Test
  void toFhirRoutingToDestination() {
    assertThat(MedicationDispenseSamples.R4.create().medicationDispenseDestination("MAILED"))
        .isEqualTo(
            _transform(
                    MedicationDispenseSamples.Vista.create()
                        .results(MedicationDispenseSamples.Vista.create().medWithRouting("M")))
                .findFirst()
                .get());

    assertThat(
            MedicationDispenseSamples.R4
                .create()
                .medicationDispenseDestination("ADMINISTERED IN CLINIC"))
        .isEqualTo(
            _transform(
                    MedicationDispenseSamples.Vista.create()
                        .results(MedicationDispenseSamples.Vista.create().medWithRouting("C")))
                .findFirst()
                .get());
  }

  @Test
  void toFhirRpcMedWithMultipleFillsAndReleaseDate() {
    Stream<MedicationDispense> dispenses =
        _transform(MedicationDispenseSamples.Vista.create().resultsWithMultipleFills());
    assertThat(dispenses.count()).isEqualTo(2);
  }
}
