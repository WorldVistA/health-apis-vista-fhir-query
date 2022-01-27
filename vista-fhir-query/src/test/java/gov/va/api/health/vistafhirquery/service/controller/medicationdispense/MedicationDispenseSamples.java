package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Duration;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Range;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.datatypes.Timing;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Fill;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.math.BigDecimal;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MedicationDispenseSamples {

  @NoArgsConstructor(staticName = "create")
  public static class R4 {

    public MedicationDispense medicationDispense() {
      return medicationDispense("sNp1+673+MYOUR_VISTA_ID_HERE");
    }

    public MedicationDispense medicationDispense(String id) {
      return MedicationDispense.builder()
          .id(id)
          .meta(Meta.builder().source("673").build())
          .identifier(List.of(Identifier.builder().value("98765A").build()))
          .quantity(SimpleQuantity.builder().value(BigDecimal.valueOf(30)).unit("TAB").build())
          .whenPrepared("2004-08-01T00:00:00Z")
          .whenHandedOver("2004-08-02T00:00:00Z")
          .status(Status.in_progress)
          .medicationCodeableConcept(
              CodeableConcept.builder()
                  .text("IBUPROFEN")
                  .coding(
                      Coding.builder()
                          .system("https://www.pbm.va.gov/nationalformulary.asp")
                          .code("MS102")
                          .display("NONSALICYLATE NSAIs,ANTIRHEUMATIC")
                          .build()
                          .asList())
                  .build())
          .daysSupply(SimpleQuantity.builder().value(BigDecimal.valueOf(30)).build())
          .subject(toReference("Patient", "p1", null))
          .performer(
              MedicationDispense.Performer.builder()
                  .actor(toReference("Practitioner", "20144", "PHARMACIST, THIRTY"))
                  .build())
          .category(
              CodeableConcept.builder()
                  .coding(
                      List.of(
                          Coding.builder()
                              .code("inpatient")
                              .display("Inpatient")
                              .system(
                                  "http://terminology.hl7.org/fhir/CodeSystem/medicationdispense-category")
                              .build()))
                  .build())
          .dosageInstruction(
              List.of(
                  Dosage.builder()
                      .text("TAKE 1 TAB BY MOUTH EVERY DAY")
                      .patientInstruction("Take with food")
                      .doseAndRate(
                          List.of(
                              Dosage.DoseAndRate.builder()
                                  .doseQuantity(
                                      SimpleQuantity.builder()
                                          .value(BigDecimal.valueOf(10))
                                          .unit("mg")
                                          .build())
                                  .build()))
                      .build()))
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class Vista {

    public Meds.Med med() {
      return med("YOUR_VISTA_ID_HERE");
    }

    public Meds.Med med(String id) {
      return Med.builder()
          .id(ValueOnlyXmlAttribute.of(id))
          .facility(CodeAndNameXmlAttribute.of("673", "TAMPA (JAH VAH)"))
          .prescription(ValueOnlyXmlAttribute.of("98765A"))
          .quantity(ValueOnlyXmlAttribute.of("30"))
          .form(ValueOnlyXmlAttribute.of("TAB"))
          .fill(List.of(Fill.builder().fillDate("3040801").releaseDate("3040802").build()))
          .status(ValueOnlyXmlAttribute.of("HOLD"))
          .product(
              List.of(
                  Product.builder()
                      .name("IBUPROFEN")
                      .clazz(
                          ProductDetail.builder()
                              .code("MS102")
                              .name("NONSALICYLATE NSAIs,ANTIRHEUMATIC")
                              .build())
                      .build()))
          .daysSupply(ValueOnlyXmlAttribute.of("30"))
          .pharmacist(
              CodeAndNameXmlAttribute.builder().code("20144").name("PHARMACIST, THIRTY").build())
          .vaType(ValueOnlyXmlAttribute.of("I"))
          .dose(
              List.of(
                  Med.Dose.builder().noun("TAB").dose("10").units("mg").unitsPerDose("1").build()))
          .sig("TAKE 1 TAB BY MOUTH EVERY DAY")
          .ptInstructions(ValueOnlyXmlAttribute.of("Take with food"))
          .build();
    }

    public List<Meds.Med> meds() {
      return List.of(med());
    }

    public VprGetPatientData.Response.Results results() {
      return results(med());
    }

    public VprGetPatientData.Response.Results results(Meds.Med med) {
      return VprGetPatientData.Response.Results.builder()
          .version("1.13")
          .timeZone("-0500")
          .meds(Meds.builder().medResults(List.of(med)).build())
          .build();
    }
  }
}
