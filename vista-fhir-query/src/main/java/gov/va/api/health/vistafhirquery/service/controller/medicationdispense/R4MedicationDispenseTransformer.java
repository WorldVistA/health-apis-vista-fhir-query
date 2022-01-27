package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformers;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
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

  @NonNull
  private VprGetPatientData.Response.Results rpcResults;

  /**
   * Vista vaType -> FHIR category.
   */
  @SuppressWarnings("UnnecessaryParentheses")
  public static CodeableConcept category(String vaType) {
    String display =
        switch (vaType) {
          case "I", "V" -> "Inpatient";
          case "O", "N" -> "Outpatient";
          default -> throw new IllegalStateException("Unexpected va type: " + vaType);
        };
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .code(display.toLowerCase())
                    .display(display)
                    .system(
                        "http://terminology.hl7.org/fhir/CodeSystem/medicationdispense-category")
                    .build()))
        .build();
  }

  /**
   * Vista Rx status -> FHIR status.
   */
  @SuppressWarnings("UnnecessaryParentheses")
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

  private SimpleQuantity daysSupply(String daysSupply) {
    return SimpleQuantity.builder().value(BigDecimal.valueOf(Long.parseLong(daysSupply))).build();
  }

  String idFrom(String id) {
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, Domains.meds, id);
  }

  List<Identifier> identifiers(ValueOnlyXmlAttribute prescription) {
    return Identifier.builder().value(prescription.value()).build().asList();
  }

  private CodeableConcept medicationCodeableConcept(Product product) {
    return CodeableConcept.builder()
        .text(product.name())
        .coding(
            List.of(
                Coding.builder()
                    .code(product.clazz().code())
                    .display(product.clazz().name())
                    .system("https://www.pbm.va.gov/nationalformulary.asp")
                    .build()))
        .build();
  }

  SimpleQuantity quantity(ValueOnlyXmlAttribute quantity, ValueOnlyXmlAttribute unit) {
    return SimpleQuantity.builder()
        .value(new BigDecimal(quantity.value()))
        .unit(unit.value())
        .build();
  }

  public String toDate(String date) {
    return R4Transformers.optionalInstantToString(toHumanDateTime(date));
  }

  public Stream<MedicationDispense> toFhir() {
    return rpcResults.medStream().map(this::toMedicationDispense).filter(Objects::nonNull);
  }

  private MedicationDispense toMedicationDispense(Meds.Med rpcMed) {
    if (rpcMed == null) {
      return null;
    }
    return MedicationDispense.builder()
        .id(idFrom(rpcMed.id().value()))
        .meta(Meta.builder().source(site).build())
        .identifier(identifiers(rpcMed.prescription()))
        .quantity(quantity(rpcMed.quantity(), rpcMed.form()))
        .whenPrepared(toDate(rpcMed.fill().get(0).fillDate()))
        .whenHandedOver(toDate(rpcMed.fill().get(0).releaseDate()))
        .status(r4PrescriptionStatus(rpcMed.status().value()))
        .medicationCodeableConcept(medicationCodeableConcept(rpcMed.product().get(0)))
        .daysSupply(daysSupply(rpcMed.daysSupply().value()))
        .subject(toReference("Patient", patientIcn, null))
        .performer(
            MedicationDispense.Performer.builder()
                .actor(
                    toReference(
                        "Practitioner", rpcMed.pharmacist().code(), rpcMed.pharmacist().name()))
                .build())
        .category(category(rpcMed.vaType().value()))
        .dosageInstruction(List.of(Dosage.builder()
            .patientInstruction(rpcMed.ptInstructions().value())
            .text(rpcMed.sig())
            .doseAndRate(List.of(Dosage.DoseAndRate.builder()
                .doseQuantity(SimpleQuantity.builder()
                    .value(BigDecimal.valueOf(Long.parseLong(rpcMed.dose().get(0).dose())))
                    .unit(rpcMed.dose().get(0).units())
                    .build())
                .build()))
            .build()))
        .build();
  }
}
