package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.Intent;
import gov.va.api.health.r4.api.resources.MedicationRequest.Status;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class R4MedicationRequestTransformer {

  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull VprGetPatientData.Response.Results rpcResults;

  String idFrom(String id) {
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, VprGetPatientData.Domains.meds, id);
  }

  /** Convert VistA Med data to R4 Fhir data. */
  public Stream<MedicationRequest> toFhir() {
    return rpcResults().medStream().filter(Med::isNotEmpty).map(this::toMedicationRequest);
  }

  /** required fields to be updated with correct logic later. * */
  private MedicationRequest toMedicationRequest(Med rpcMed) {
    return MedicationRequest.builder()
        .id(idFrom(rpcMed.id().value()))
        .meta(Meta.builder().source(site()).build())
        .subject(toReference("Patient", patientIcn, null))
        .intent(Intent.order)
        .authoredOn("2011-05-08T00:00:00Z")
        .status(Status.active)
        .medicationCodeableConcept(CodeableConcept.builder().build())
        .requester(Reference.builder().build())
        .build();
  }
}
