package gov.va.api.health.vistafhirquery.service.controller.observation;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.lighthouse.vistalink.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.vistalink.models.vprgetpatientdata.Vitals;
import gov.va.api.lighthouse.vistalink.models.vprgetpatientdata.VprGetPatientData;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObservationSamples {
  @AllArgsConstructor(staticName = "create")
  public static class Vista {
    public List<Vitals.Measurement> measurements() {
      return List.of(
          Vitals.Measurement.builder()
              .id("32071")
              .vuid("4500634")
              .name("BLOOD PRESSURE")
              .value("126/65")
              .units("mm[Hg]")
              .high("210/110")
              .low("100/60")
              .build(),
          Vitals.Measurement.builder()
              .id("32076")
              .vuid("4500639")
              .name("WEIGHT")
              .value("190")
              .units("lb")
              .metricValue("86.18")
              .metricUnits("kg")
              .bmi("25")
              .build());
    }

    public Map.Entry<String, VprGetPatientData.Response.Results> resultsByStation() {
      return Map.entry(
          "673",
          VprGetPatientData.Response.Results.builder()
              .version("1.13")
              .timeZone("-0500")
              .vitals(Vitals.builder().total(1).vitalResults(vitals()).build())
              .build());
    }

    public List<Vitals.Vital> vitals() {
      return List.of(
          Vitals.Vital.builder()
              .entered(ValueOnlyXmlAttribute.builder().value("3110225.110428").build())
              .facility(
                  CodeAndNameXmlAttribute.builder().code("673").name("TAMPA (JAH VAH)").build())
              .location(
                  CodeAndNameXmlAttribute.builder().code("23").name("GENERAL MEDICINE").build())
              .measurements(measurements())
              .taken(ValueOnlyXmlAttribute.builder().value("3100406.14").build())
              .build());
    }
  }

  @AllArgsConstructor(staticName = "create")
  public static class Fhir {
    public Observation bloodPressure() {
      return Observation.builder()
          .resourceType("Observation")
          .category(category())
          .code(bloodPressureCode())
          .component(List.of(bloodPressureSystolic(), bloodPressureDiastolic()))
          .effectiveDateTime("3100406.14")
          .issued("3110225.110428")
          .id("32071")
          .performer(performer())
          .status(Observation.ObservationStatus._final)
          .build();
    }

    public CodeableConcept bloodPressureCode() {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .system("http://loinc.org")
                      .code("4500634")
                      .display("BLOOD PRESSURE")
                      .build()))
          .build();
    }

    public Observation.Component bloodPressureDiastolic() {
      return Observation.Component.builder()
          .referenceRange(
              List.of(
                  Observation.ReferenceRange.builder()
                      .high(SimpleQuantity.builder().value(new BigDecimal("110")).build())
                      .low(SimpleQuantity.builder().value(new BigDecimal("60")).build())
                      .build()))
          .valueQuantity(Quantity.builder().value(new BigDecimal("65")).unit("mm[Hg]").build())
          .build();
    }

    public Observation.Component bloodPressureSystolic() {
      return Observation.Component.builder()
          .referenceRange(
              List.of(
                  Observation.ReferenceRange.builder()
                      .high(SimpleQuantity.builder().value(new BigDecimal("210")).build())
                      .low(SimpleQuantity.builder().value(new BigDecimal("100")).build())
                      .build()))
          .valueQuantity(Quantity.builder().value(new BigDecimal("126")).unit("mm[Hg]").build())
          .build();
    }

    public List<CodeableConcept> category() {
      return List.of(
          CodeableConcept.builder()
              .text("Vital Signs")
              .coding(
                  List.of(
                      Coding.builder()
                          .code("vital-signs")
                          .display("Vital Signs")
                          .system("http://terminology.hl7.org/CodeSystem/observation-category")
                          .build()))
              .build());
    }

    List<Observation> observations() {
      return List.of(bloodPressure(), weight());
    }

    public List<Reference> performer() {
      return List.of(Reference.builder().reference("673").display("TAMPA (JAH VAH)").build());
    }

    public Observation weight() {
      return Observation.builder()
          .resourceType("Observation")
          .category(category())
          .code(weightCode())
          .effectiveDateTime("3100406.14")
          .issued("3110225.110428")
          .id("32076")
          .performer(performer())
          .status(Observation.ObservationStatus._final)
          .valueQuantity(Quantity.builder().value(new BigDecimal("190")).unit("lb").build())
          .build();
    }

    public CodeableConcept weightCode() {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .system("http://loinc.org")
                      .code("4500639")
                      .display("WEIGHT")
                      .build()))
          .build();
    }
  }
}
