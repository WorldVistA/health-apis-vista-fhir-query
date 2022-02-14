package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.Intent;
import gov.va.api.health.r4.api.resources.MedicationRequest.Status;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformers;
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
          .subject(R4Transformers.toReference("Patient", "p1", null))
          .intent(Intent.order)
          .authoredOn("2011-05-08T00:00:00Z")
          .status(Status.active)
          .medicationCodeableConcept(CodeableConcept.builder().build())
          .requester(Reference.builder().build())
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class Vista {

    public Med med() {
      return med("33714");
    }

    public Med med(String id) {
      return Med.builder()
          .id(ValueOnlyXmlAttribute.of(id))
          .facility(CodeAndNameXmlAttribute.of("673", "TAMPA (JAH VAH)"))
          .build();
    }

    public VprGetPatientData.Response.Results results() {
      return results(med());
    }

    public VprGetPatientData.Response.Results results(Med med) {
      return VprGetPatientData.Response.Results.builder()
          .version("1.13")
          .timeZone("-0500")
          .meds(Meds.builder().medResults(List.of(med)).build())
          .build();
    }
  }
}
