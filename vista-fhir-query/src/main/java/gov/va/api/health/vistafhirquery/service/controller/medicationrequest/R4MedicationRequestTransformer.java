package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.optionalInstantToString;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toBigDecimal;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.valueOfValueOnlyXmlAttribute;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.DispenseRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.Intent;
import gov.va.api.health.r4.api.resources.MedicationRequest.Status;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
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

  /** Return the appropriate med category for the corresponding VistA type. */
  @SuppressWarnings("UnnecessaryParentheses")
  private List<CodeableConcept> category(String vaType) {
    if (isBlank(vaType)) {
      return null;
    }
    String display =
        switch (vaType) {
          case "I" -> "Inpatient";
          case "O" -> "Outpatient";
          case "N", "V" -> "Unknown";
          default -> throw new IllegalStateException("Unexpected va type: " + vaType);
        };
    return List.of(
        CodeableConcept.builder()
            .text(display)
            .coding(
                List.of(
                    Coding.builder()
                        .code(display.toLowerCase())
                        .display(display)
                        .system(
                            "http://terminology.hl7.org/fhir/CodeSystem/medicationrequest-category")
                        .build()))
            .build());
  }

  private DispenseRequest dispenseRequest(
      String start, String end, String refills, String quantity, String form) {
    SimpleQuantity simpleQuantity = quantity(quantity, form);
    if (allBlank(start, end, refills, simpleQuantity)) {
      return null;
    }
    return DispenseRequest.builder()
        .validityPeriod(Period.builder().start(start).end(end).build())
        .numberOfRepeatsAllowed(refills != null ? Integer.parseInt(refills) : null)
        .quantity(simpleQuantity)
        .build();
  }

  private List<Dosage> dosageInstruction(String sig, String ptInstructions) {
    if (allBlank(sig, ptInstructions)) {
      return null;
    }
    return Dosage.builder().text(sig).patientInstruction(ptInstructions).build().asList();
  }

  String idFrom(String id) {
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, VprGetPatientData.Domains.meds, id);
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

  private SimpleQuantity quantity(String quantity, String form) {
    if (isBlank(quantity) || isBlank(form)) {
      return null;
    }
    return SimpleQuantity.builder().value(toBigDecimal(quantity)).unit(form).build();
  }

  private Reference requester(Med.Provider orderingProvider) {
    if (isBlank(orderingProvider)) {
      return null;
    }
    return toReference("Practitioner", null, orderingProvider.name());
  }

  /** Return the appropriate FHIR status for the corresponding VistA vaStatus. */
  @SuppressWarnings("UnnecessaryParentheses")
  public Status status(String vistaStatus) {
    if (vistaStatus == null) {
      return Status.unknown;
    }
    return switch (vistaStatus) {
      case "HOLD", "PROVIDER HOLD", "ACTIVE", "SUSPENDED" -> Status.active;
      case "DRUG INTERACTIONS", "NON-VERIFIED" -> Status.draft;
      case "DISCONTINUED", "DISCONTINUED (EDIT)", "DISCONTINUED BY PROVIDER" -> Status.stopped;
      case "DELETED" -> Status.entered_in_error;
      case "EXPIRED" -> Status.completed;
      default -> throw new IllegalStateException("Unexpected prescription status: " + vistaStatus);
    };
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
        .authoredOn(optionalInstantToString(toHumanDateTime(rpcMed.ordered())))
        .status(status(valueOfValueOnlyXmlAttribute(rpcMed.vaStatus())))
        .category(category(valueOfValueOnlyXmlAttribute(rpcMed.vaType())))
        .medicationCodeableConcept(
            Safe.stream(rpcMed.product())
                .findFirst()
                .map(this::medicationCodeableConcept)
                .orElse(null))
        .requester(requester(rpcMed.orderingProvider()))
        .dispenseRequest(
            dispenseRequest(
                optionalInstantToString(toHumanDateTime(rpcMed.ordered())),
                optionalInstantToString(toHumanDateTime(rpcMed.expires())),
                valueOfValueOnlyXmlAttribute(rpcMed.fillsAllowed()),
                valueOfValueOnlyXmlAttribute(rpcMed.quantity()),
                valueOfValueOnlyXmlAttribute(rpcMed.form())))
        .dosageInstruction(
            dosageInstruction(rpcMed.sig(), valueOfValueOnlyXmlAttribute(rpcMed.ptInstructions())))
        .build();
  }
}
