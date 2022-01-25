package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;

import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformers;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4MedicationDispenseTransformer {

  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull private VprGetPatientData.Response.Results rpcResults;

  String idFrom(String id) {
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, VprGetPatientData.Domains.meds, id);
  }

  List<Identifier> identifiers(ValueOnlyXmlAttribute prescription) {
    return Identifier.builder().value(prescription.value()).build().asList();
  }

  SimpleQuantity quantity(ValueOnlyXmlAttribute quantity, ValueOnlyXmlAttribute unit) {
    return SimpleQuantity.builder()
        .value(new BigDecimal(quantity.value()))
        .unit(unit.value())
        .build();
  }

  public Stream<MedicationDispense> toFhir() {
    return rpcResults.medStream().map(this::toMedicationDispense).filter(Objects::nonNull);
  }

  public String toDate(String date) {
    return R4Transformers.optionalInstantToString(toHumanDateTime(date));
  }

  private MedicationDispense toMedicationDispense(Meds.Med rpcMed) {
    if (rpcMed == null) {
      return null;
    }
    return MedicationDispense.builder()
        .meta(Meta.builder().source(site).build())
        .identifier(identifiers(rpcMed.prescription()))
        .quantity(quantity(rpcMed.quantity(), rpcMed.form()))
        .whenPrepared(toDate(rpcMed.fill().get(0).fillDate()))
        .whenHandedOver(toDate(rpcMed.fill().get(0).releaseDate()))
        .status(r4PrescriptionStatus(rpcMed.status().value()))
        .build();
  }

  public static Status r4PrescriptionStatus(String status) {
    return switch (status) {
      case "HOLD", "PROVIDER HOLD", "ACTIVE", "SUSPENDED" -> Status.in_progress;
      case "DRUG INTERACTIONS", "NON VERIFIED" -> Status.preparation;
      case "DISCONTINUED", "DISCONTINUED (EDIT)", "DISCONTINUED BY PROVIDER" -> Status.stopped;
      case "DELETED" -> Status.entered_in_error;
      case "EXPIRED" -> Status.completed;
      default -> throw new IllegalStateException("Unexpected prescription status: " + status);
    };
  }
}
