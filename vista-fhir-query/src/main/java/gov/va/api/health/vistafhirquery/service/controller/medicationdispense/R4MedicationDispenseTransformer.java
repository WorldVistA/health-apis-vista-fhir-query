package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toBigDecimal;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.valueOfValueOnlyXmlAttribute;
import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformers;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier.PatientIdentifierType;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Fill;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class R4MedicationDispenseTransformer {

  private static final Map<String, String> ROUTING_TO_DESTINATION_MAPPING =
      Map.of("W", "WINDOW", "M", "MAILED", "C", "ADMINISTERED IN CLINIC");

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
    var value = valueOfValueOnlyXmlAttribute(routing);
    if (isBlank(value)) {
      return null;
    }
    var display = ROUTING_TO_DESTINATION_MAPPING.get(value);
    if (isBlank(display)) {
      return null;
    }
    return Reference.builder().display(display).build();
  }

  List<Dosage> dosageInstruction(String sig, String ptInstructions) {
    if (allBlank(sig, ptInstructions)) {
      return null;
    }
    return Dosage.builder()
        .text(trimToNull(sig))
        .patientInstruction(trimToNull(ptInstructions))
        .build()
        .asList();
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

  CodeableConcept medicationCodeableConcept(Product product) {

    if (product == null || allBlank(product.name(), product.clazz())) {
      return null;
    }
    return CodeableConcept.builder()
        .text(trimToNull(product.name()))
        .coding(productCoding(product.clazz()))
        .build();
  }

  List<Coding> productCoding(ProductDetail maybeDetail) {
    if (isBlank(maybeDetail)) {
      return null;
    }
    if (allBlank(maybeDetail.code(), maybeDetail.name())) {
      return null;
    }
    return Coding.builder()
        .system("https://www.pbm.va.gov/nationalformulary.asp")
        .code(maybeDetail.code())
        .display(maybeDetail.name())
        .build()
        .asList();
  }

  private SimpleQuantity quantity(String fillQuantity) {
    /*
     * https://vivian.worldvista.org/dox/SubFile_52.1.html `52.1-1` `QTY` may have alpha
     * characters for entered prior to 2000-02-17.s
     */
    var value = toBigDecimal(fillQuantity);
    if (isBlank(value)) {
      return null;
    }
    return SimpleQuantity.builder().value(value).build();
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
        .subject(toReference("Patient", patientIcn(), null))
        .medicationCodeableConcept(
            Safe.stream(rpcMed.product())
                .findFirst()
                .map(this::medicationCodeableConcept)
                .orElse(null))
        .whenHandedOver(toDate(fill.releaseDate()))
        .quantity(quantity(fill.fillQuantity()))
        .daysSupply(daysSupply(fill.fillDaysSupply()))
        .whenPrepared(toDate(fill.fillDate()))
        .destination(destination(rpcMed.routing()))
        .dosageInstruction(
            dosageInstruction(rpcMed.sig(), valueOfValueOnlyXmlAttribute(rpcMed.ptInstructions())))
        .build();
  }
}
