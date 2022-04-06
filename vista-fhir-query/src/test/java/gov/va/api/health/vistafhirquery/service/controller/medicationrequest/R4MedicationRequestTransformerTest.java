package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationRequest.DispenseRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.Status;
import gov.va.api.health.vistafhirquery.service.controller.medication.MedicationSamples;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class R4MedicationRequestTransformerTest {
  static Stream<Arguments> category() {
    Function<String, CodeableConcept> cc =
        display ->
            CodeableConcept.builder()
                .text(display)
                .coding(
                    Coding.builder()
                        .code(display.toLowerCase())
                        .display(display)
                        .system(
                            "http://terminology.hl7.org/fhir/CodeSystem/medicationrequest-category")
                        .build()
                        .asList())
                .build();
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of("", null),
        Arguments.of("NOPE", null),
        Arguments.of("I", cc.apply("Inpatient").asList()),
        Arguments.of("O", cc.apply("Outpatient").asList()));
  }

  static Stream<Arguments> dispenseRequest() {
    BiFunction<BigDecimal, String, SimpleQuantity> quantity =
        (v, u) -> SimpleQuantity.builder().value(v).unit(u).build();
    BiFunction<String, String, Period> period = (s, e) -> Period.builder().start(s).end(e).build();
    return Stream.of(
        Arguments.of(null, null, null, null, null, null),
        Arguments.of("", "", "", "", "", null),
        Arguments.of(
            "2005",
            "2006",
            "2",
            "99",
            "pills",
            DispenseRequest.builder()
                .validityPeriod(period.apply("2005", "2006"))
                .numberOfRepeatsAllowed(2)
                .quantity(quantity.apply(new BigDecimal(99), "pills"))
                .build()),
        Arguments.of(
            "2005",
            null,
            "2",
            "99",
            null,
            DispenseRequest.builder()
                .validityPeriod(period.apply("2005", null))
                .numberOfRepeatsAllowed(2)
                .quantity(quantity.apply(new BigDecimal(99), null))
                .build()),
        Arguments.of(
            "2005",
            "2006",
            null,
            null,
            "pills",
            DispenseRequest.builder().validityPeriod(period.apply("2005", "2006")).build()),
        Arguments.of(
            null,
            null,
            "2",
            "99",
            "pills",
            DispenseRequest.builder()
                .numberOfRepeatsAllowed(2)
                .quantity(quantity.apply(new BigDecimal(99), "pills"))
                .build()));
  }

  static Stream<Arguments> requester() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(Med.Provider.builder().build(), null),
        Arguments.of(Med.Provider.builder().code("ignored").build(), null),
        Arguments.of(
            Med.Provider.builder().name("fugazi").build(),
            Reference.builder().display("fugazi").build()));
  }

  static Stream<Arguments> status() {
    return Stream.of(
        Arguments.of("HOLD", Status.active, statusExtension("HOLD", "3")),
        Arguments.of("PROVIDER HOLD", Status.active, statusExtension("PROVIDER HOLD", "16")),
        Arguments.of("DRUG INTERACTIONS", Status.draft, statusExtension("DRUG INTERACTIONS", "4")),
        Arguments.of("NON-VERIFIED", Status.draft, statusExtension("NON-VERIFIED", "1")),
        Arguments.of("ACTIVE", Status.active, statusExtension("ACTIVE", "0")),
        Arguments.of("SUSPENDED", Status.active, statusExtension("SUSPENDED", "5")),
        Arguments.of("DISCONTINUED", Status.stopped, statusExtension("DISCONTINUED", "12")),
        Arguments.of(
            "DISCONTINUED (EDIT)", Status.stopped, statusExtension("DISCONTINUED (EDIT)", "15")),
        Arguments.of(
            "DISCONTINUED BY PROVIDER",
            Status.stopped,
            statusExtension("DISCONTINUED BY PROVIDER", "14")),
        Arguments.of("DELETED", Status.entered_in_error, statusExtension("DELETED", "13")),
        Arguments.of("EXPIRED", Status.completed, statusExtension("EXPIRED", "11")),
        Arguments.of(null, Status.unknown, null),
        Arguments.of("fugazi", Status.unknown, null),
        Arguments.of("", Status.unknown, null));
  }

  private static Extension statusExtension(String vaStatus, String code) {
    return Extension.builder()
        .url("http://va.gov/fhir/StructureDefinition/medicationrequest-pharmacyOrderStatus")
        .valueCodeableConcept(
            CodeableConcept.builder()
                .coding(
                    Coding.builder()
                        .code(code)
                        .display(vaStatus)
                        .system("http://va.gov/fhir/ValueSet/VistAPharmacyOrderStatus")
                        .build()
                        .asList())
                .text(vaStatus)
                .build())
        .build();
  }

  private R4MedicationRequestTransformer _transformer(VprGetPatientData.Response.Results results) {
    return R4MedicationRequestTransformer.builder()
        .site("673")
        .patientIcn("p1")
        .rpcResults(results)
        .build();
  }

  private R4MedicationRequestTransformer _transformer() {
    return _transformer(MedicationSamples.Vista.create().results());
  }

  @ParameterizedTest
  @MethodSource
  void category(String vaType, List<CodeableConcept> expected) {
    var actual = _transformer().category(vaType);
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource
  void dispenseRequest(
      String start,
      String end,
      String refills,
      String quantity,
      String form,
      DispenseRequest expected) {
    var actual = _transformer().dispenseRequest(start, end, refills, quantity, form);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void empty() {
    assertThat(_transformer(VprGetPatientData.Response.Results.builder().build()).toFhir())
        .isEmpty();
  }

  @ParameterizedTest
  @MethodSource
  void requester(Med.Provider orderingProvider, Reference expected) {
    var actual = _transformer().requester(orderingProvider);
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource
  void status(String status, Status expectedFhirStatus, Extension expectedVaStatus) {
    assertThat(_transformer().fhirStatus(status)).isEqualTo(expectedFhirStatus);
    assertThat(_transformer().vistaStatus(status)).isEqualTo(expectedVaStatus);
  }

  @Test
  void toFhir() {
    assertThat(_transformer(MedicationSamples.Vista.create().results()).toFhir())
        .containsOnly(MedicationRequestSamples.R4.create().medicationRequest());
  }
}
