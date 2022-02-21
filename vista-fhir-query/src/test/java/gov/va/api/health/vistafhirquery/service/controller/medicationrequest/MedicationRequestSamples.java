package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.DispenseRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.Intent;
import gov.va.api.health.r4.api.resources.MedicationRequest.Status;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MedicationRequestSamples {

  @NoArgsConstructor(staticName = "create")
  public static class R4 {

    public static MedicationRequest.Bundle asBundle(
        String baseUrl,
        Collection<MedicationRequest> resources,
        int totalRecords,
        BundleLink... links) {
      return MedicationRequest.Bundle.builder()
          .resourceType("Bundle")
          .total(totalRecords)
          .link(Arrays.asList(links))
          .type(AbstractBundle.BundleType.searchset)
          .entry(
              resources.stream()
                  .map(
                      resource ->
                          MedicationRequest.Entry.builder()
                              .fullUrl(baseUrl + "/MedicationRequest/" + resource.id())
                              .resource(resource)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(toList()))
          .build();
    }

    public MedicationRequest medicationRequest() {
      return medicationRequest("sNp1+673+M33714");
    }

    public MedicationRequest medicationRequest(String id) {
      return MedicationRequest.builder()
          .id(id)
          .meta(Meta.builder().source("673").build())
          .subject(toReference("Patient", "p1", null))
          .intent(Intent.order)
          .authoredOn("2011-03-01T11:32:17Z")
          .status(Status.stopped)
          .medicationCodeableConcept(CodeableConcept.builder().build())
          .requester(toReference("Practitioner", null, "HEMANN, LYNDA"))
          .category(
              List.of(
                  CodeableConcept.builder()
                      .text("Inpatient")
                      .coding(
                          List.of(
                              Coding.builder()
                                  .code("inpatient")
                                  .display("Inpatient")
                                  .system(
                                      "http://terminology.hl7.org/fhir/CodeSystem/medicationrequest-category")
                                  .build()))
                      .build()))
          .medicationCodeableConcept(
              CodeableConcept.builder()
                  .text("WARFARIN")
                  .coding(
                      List.of(
                          Coding.builder()
                              .system("https://www.pbm.va.gov/nationalformulary.asp")
                              .code("BL110")
                              .display("ANTICOAGULANTS")
                              .build()))
                  .build())
          .dispenseRequest(
              DispenseRequest.builder()
                  .validityPeriod(
                      Period.builder()
                          .start("2011-03-01T11:32:17Z")
                          .end("2012-03-01T00:00:00Z")
                          .build())
                  .numberOfRepeatsAllowed(3)
                  .quantity(
                      SimpleQuantity.builder().value(BigDecimal.valueOf(30)).unit("TAB").build())
                  .build())
          .dosageInstruction(
              Dosage.builder()
                  .text("TAKE 1 TAB BY MOUTH EVERY DAY")
                  .patientInstruction("take with food")
                  .build()
                  .asList())
          .build();
    }
  }
}
