package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toBigDecimal;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.valueOfValueOnlyXmlAttribute;
import static io.micrometer.core.instrument.util.StringUtils.isNotBlank;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
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
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class R4MedicationDispenseTransformer {
  private static final Map<String, String> ROUTING_TO_DESTINATION_MAPPING =
      Map.of("W", "WINDOW", "M", "MAILED", "C", "ADMINISTERED IN CLINIC");

  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull Optional<String> fillDateFilter;

  @NonNull VprGetPatientData.Response.Results rpcResults;

  private SimpleQuantity daysSupply(String fillDaysSupply) {
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

  private Reference destination(ValueOnlyXmlAttribute routing) {
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

  private boolean hasRequiredFillDate(Fill fill) {
    if (fillDateFilter().isEmpty()) {
      /* No filter specified, everything is good. */
      return true;
    }
    return fillDateFilter().get().equals(fill.fillDate());
  }

  private MedicationDispenseId idOf(Med rpcMed, Fill fill) {
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

  private boolean isViable(Fill fill) {
    /* Fill date is essential for the resulting medication dispense record. */
    return isNotBlank(fill.fillDate());
  }

  private CodeableConcept medicationCodeableConcept(Product product) {
    if (product == null || allBlank(product.name(), product.clazz())) {
      return null;
    }
    return CodeableConcept.builder()
        .text(product.name())
        .coding(productCoding(product.clazz()))
        .build();
  }

  private List<Coding> productCoding(ProductDetail maybeDetail) {
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

  private Status status(Fill fill) {
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
                med.fill().stream()
                    .filter(Objects::nonNull)
                    .filter(this::isViable)
                    .filter(this::hasRequiredFillDate)
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
        .build();
  }
}
