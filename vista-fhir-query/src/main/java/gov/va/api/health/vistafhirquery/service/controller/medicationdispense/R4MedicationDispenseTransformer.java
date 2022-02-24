package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asListOrNull;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toBigDecimal;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.valueOfValueOnlyXmlAttribute;
import static gov.va.api.health.vistafhirquery.service.controller.medication.R4MedicationTransformers.dosageInstruction;
import static gov.va.api.health.vistafhirquery.service.controller.medication.R4MedicationTransformers.medicationRequestIdFrom;
import static gov.va.api.health.vistafhirquery.service.controller.medication.R4MedicationTransformers.quantity;
import static gov.va.api.health.vistafhirquery.service.util.Translations.ignoreAndReturnNull;
import static gov.va.api.health.vistafhirquery.service.util.Translations.returnNull;
import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformers;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier.PatientIdentifierType;
import gov.va.api.health.vistafhirquery.service.controller.medication.R4MedicationTransformers;
import gov.va.api.health.vistafhirquery.service.util.Translation;
import gov.va.api.health.vistafhirquery.service.util.Translations;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Fill;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class R4MedicationDispenseTransformer {

  private static final Translation<String, String> ROUTING_TO_DESTINATION_DISPLAY =
      Translations.ofStringToString()
          .from("W")
          .to("WINDOW")
          .from("M")
          .to("MAILED")
          .from("C")
          .to("ADMINISTERED IN CLINIC")
          .whenNullOrEmpty(returnNull())
          .whenNotFound(ignoreAndReturnNull())
          .build();

  @NonNull final String site;

  @NonNull final String patientIcn;

  @NonNull final Predicate<Fill> fillFilter;

  @NonNull final VprGetPatientData.Response.Results rpcResults;

  public static Predicate<Fill> acceptAll() {
    return fill -> true;
  }

  public static Predicate<Fill> acceptOnlyWithFillDateEqualTo(@NonNull String onlyThisFillDate) {
    return fill -> onlyThisFillDate.equals(fill.fillDate());
  }

  public static Predicate<Fill> acceptOnlyWithFillDateInRange(@NonNull DateSearchBoundaries range) {
    return fill -> toHumanDateTime(fill.fillDate()).map(range::isDateWithinBounds).orElse(false);
  }

  List<Reference> authorizingPrescription(String vistaMedId) {
    if (isBlank(vistaMedId)) {
      return null;
    }
    Reference reference =
        toReference(
            "MedicationRequest", medicationRequestIdFrom(vistaMedId, patientIcn, site), null);
    return isBlank(reference) ? null : reference.asList();
  }

  SimpleQuantity daysSupply(String fillDaysSupply) {
    var value = toBigDecimal(fillDaysSupply);
    if (isBlank(value)) {
      return null;
    }
    return SimpleQuantity.builder()
        .system("http://unitsofmeasure.org")
        .code("d")
        .unit("day")
        .value(value)
        .build();
  }

  Reference destination(ValueOnlyXmlAttribute routing) {
    return ROUTING_TO_DESTINATION_DISPLAY
        .translate(valueOfValueOnlyXmlAttribute(routing))
        .map(display -> Reference.builder().display(display).build())
        .orElse(null);
  }

  MedicationDispenseId idOf(Med rpcMed, Fill fill) {
    return MedicationDispenseId.builder()
        .vistaId(
            SegmentedVistaIdentifier.builder()
                .patientIdentifierType(PatientIdentifierType.NATIONAL_ICN)
                .patientIdentifier(patientIcn())
                .vprRpcDomain(Domains.meds)
                .siteId(site())
                .recordId(rpcMed.id().value())
                .build())
        .fillDate(fill.fillDate())
        .build();
  }

  boolean isViable(Fill fill) {
    /* Fill date is essential for the resulting medication dispense record. */
    return isNotBlank(fill.fillDate());
  }

  Status status(Fill fill) {
    if (isBlank(fill.releaseDate())) {
      return Status.in_progress;
    }
    return Status.completed;
  }

  private String toDate(String date) {
    return R4Transformers.optionalInstantToString(toHumanDateTime(date));
  }

  /** Convert VistA Med data to R4 Fhir data. */
  public Stream<MedicationDispense> toFhir() {
    return rpcResults()
        .medStream()
        .filter(Med::isNotEmpty)
        .flatMap(
            med ->
                Safe.stream(med.fill())
                    .filter(Objects::nonNull)
                    .filter(this::isViable)
                    .filter(fillFilter())
                    .map(fill -> toMedicationDispense(med, fill)));
  }

  private MedicationDispense toMedicationDispense(Med rpcMed, Fill fill) {
    return MedicationDispense.builder()
        .id(idOf(rpcMed, fill).toString())
        .meta(Meta.builder().source(site()).build())
        .status(status(fill))
        .subject(toReference("Patient", patientIcn, null))
        .medicationCodeableConcept(
            Safe.stream(rpcMed.product())
                .findFirst()
                .map(R4MedicationTransformers::medicationCodeableConcept)
                .orElse(null))
        .whenHandedOver(toDate(fill.releaseDate()))
        .quantity(quantity(fill.fillQuantity(), null))
        .daysSupply(daysSupply(fill.fillDaysSupply()))
        .whenPrepared(toDate(fill.fillDate()))
        .destination(destination(rpcMed.routing()))
        .dosageInstruction(
            asListOrNull(
                dosageInstruction(
                    rpcMed.sig(), valueOfValueOnlyXmlAttribute(rpcMed.ptInstructions()))))
        .authorizingPrescription(authorizingPrescription(valueOfValueOnlyXmlAttribute(rpcMed.id())))
        .build();
  }
}
