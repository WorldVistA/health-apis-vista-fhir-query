package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Dosage.DoseAndRate;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MedicationDispenseSamples {

  @NoArgsConstructor(staticName = "create")
  public static class R4 {

    public static MedicationDispense.Bundle asBundle(
        String baseUrl,
        Collection<MedicationDispense> resources,
        int totalRecords,
        BundleLink... links) {
      return MedicationDispense.Bundle.builder()
          .resourceType("Bundle")
          .total(totalRecords)
          .link(Arrays.asList(links))
          .type(AbstractBundle.BundleType.searchset)
          .entry(
              resources.stream()
                  .map(
                      resource ->
                          MedicationDispense.Entry.builder()
                              .fullUrl(baseUrl + "/MedicationDispense/" + resource.id())
                              .resource(resource)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .toList())
          .build();
    }

    public MedicationDispense medicationDispense() {
      return medicationDispense("sNp1+673+M33714:3110507");
    }

    public MedicationDispense medicationDispense(String id) {
      return MedicationDispense.builder()
          .id(id)
          .meta(Meta.builder().source("673").build())
          .status(Status.completed)
          .subject(toReference("Patient", "p1", null))
          .medicationCodeableConcept(
              CodeableConcept.builder()
                  .text("WARFARIN")
                  .coding(
                      Coding.builder()
                          .system("https://www.pbm.va.gov/nationalformulary.asp")
                          .code("BL110")
                          .display("ANTICOAGULANTS")
                          .build()
                          .asList())
                  .build())
          .whenHandedOver("2011-05-08T00:00:00Z")
          .quantity(SimpleQuantity.builder().value(new BigDecimal("30")).build())
          .daysSupply(
              SimpleQuantity.builder()
                  .system("http://unitsofmeasure.org")
                  .code("d")
                  .unit("day")
                  .value(new BigDecimal("8"))
                  .build())
          .whenPrepared("2011-05-07T00:00:00Z")
          .destination(Reference.builder().display("WINDOW").build())
          .dosageInstruction(
              Dosage.builder()
                  .text("TAKE 1 TAB BY MOUTH EVERY DAY")
                  .patientInstruction("take with food")
                  .doseAndRate(
                      DoseAndRate.builder()
                          .doseQuantity(
                              SimpleQuantity.builder()
                                  .value(BigDecimal.valueOf(1.0))
                                  .unit("mg")
                                  .build())
                          .build()
                          .asList())
                  .build()
                  .asList())
          .authorizingPrescription(
              toReference("MedicationRequest", "sNp1+673+M33714", null).asList())
          .extension(
              Extension.builder()
                  .url(
                      "http://hl7.org/fhir/StructureDefinition/medicationdispense-refillsRemaining")
                  .valueInteger(3)
                  .build()
                  .asList())
          .location(Reference.builder().display("TAMPA (JAH VAH)").build())
          .build();
    }
  }
}
