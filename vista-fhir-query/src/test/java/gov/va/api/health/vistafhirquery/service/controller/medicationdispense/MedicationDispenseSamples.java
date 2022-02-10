package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformers;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Fill;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
                  .collect(toList()))
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
          .subject(R4Transformers.toReference("Patient", "p1", null))
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
          .whenHandedOver("2011-05-08T00:00:00Z")
          .build();
    }

    public MedicationDispense medicationDispenseInProgress() {
      return medicationDispense().status(Status.in_progress).whenHandedOver(null);
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class Vista {

    public Meds.Med med() {
      return med("33714");
    }

    public Meds.Med med(String id) {
      return Med.builder()
          .id(ValueOnlyXmlAttribute.of(id))
          .facility(CodeAndNameXmlAttribute.of("673", "TAMPA (JAH VAH)"))
          .fill(
              List.of(
                  Fill.builder()
                      .fillDate("3110507")
                      .fillRouting("W")
                      .releaseDate("3110508")
                      .build()))
          .product(
              List.of(
                  Product.builder()
                      .name("WARFARIN")
                      .clazz(ProductDetail.builder().name("ANTICOAGULANTS").code("BL110").build())
                      .build()))
          .build();
    }

    public Meds.Med medWithEmptyFill() {
      return med().fill(List.of(Fill.builder().build()));
    }

    private Meds.Med medWithMultipleFills() {
      return med()
          .fill(
              List.of(
                  Fill.builder().fillDate("3080728").releaseDate("3080728").build(),
                  Fill.builder().releaseDate("3091208").build(),
                  Fill.builder().fillDate("3110507").build(),
                  Fill.builder().build()));
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

    public VprGetPatientData.Response.Results resultsWithEmptyProduct() {
      return results(med().product(List.of()));
    }

    public VprGetPatientData.Response.Results resultsWithMultipleFills() {
      return results().meds(Meds.builder().medResults(List.of(medWithMultipleFills())).build());
    }

    public VprGetPatientData.Response.Results resultsWithNullProductClazz() {
      return results(med().product(List.of(med().product().get(0).clazz(null))));
    }

    public Meds.Med unreleasedMed() {
      return med().fill(List.of(Fill.builder().fillDate("3110507").fillRouting("W").build()));
    }
  }
}
