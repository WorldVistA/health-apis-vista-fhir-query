package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
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
      return medicationDispense("sNp1+673+M33714");
    }

    public MedicationDispense medicationDispense(String id) {
      return MedicationDispense.builder()
          .id(id)
          .meta(Meta.builder().source("673").build())
          .status(Status.completed)
          .medicationCodeableConcept(CodeableConcept.builder().build())
          .build();
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
          .build();
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
