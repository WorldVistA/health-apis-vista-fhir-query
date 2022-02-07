package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.valueOfValueOnlyXmlAttribute;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4MedicationDispenseTransformer {

  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull private VprGetPatientData.Response.Results rpcResults;

  String idFrom(ValueOnlyXmlAttribute maybeId) {
    String id = valueOfValueOnlyXmlAttribute(maybeId);
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, VprGetPatientData.Domains.meds, id);
  }

  public Stream<MedicationDispense> toFhir() {
    return rpcResults.medStream().filter(Med::isNotEmpty).map(this::toMedicationDispense);
  }

  private MedicationDispense toMedicationDispense(Med rpcMed) {
    return MedicationDispense.builder()
        .id(idFrom(rpcMed.id()))
        .meta(Meta.builder().source(site).build())
        .status(Status.completed)
        .medicationCodeableConcept(CodeableConcept.builder().build())
        .build();
  }
}
