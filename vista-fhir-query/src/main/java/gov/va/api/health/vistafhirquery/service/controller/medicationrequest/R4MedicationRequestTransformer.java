package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asListOrNull;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.optionalInstantToString;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toInteger;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.valueOfValueOnlyXmlAttribute;
import static gov.va.api.health.vistafhirquery.service.util.Translations.ignoreAndReturnNull;
import static gov.va.api.health.vistafhirquery.service.util.Translations.ignoreAndReturnValue;
import static gov.va.api.health.vistafhirquery.service.util.Translations.returnNull;
import static gov.va.api.health.vistafhirquery.service.util.Translations.returnValue;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.DispenseRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.Intent;
import gov.va.api.health.r4.api.resources.MedicationRequest.Status;
import gov.va.api.health.vistafhirquery.service.controller.medication.R4MedicationTransformers;
import gov.va.api.health.vistafhirquery.service.util.Translation;
import gov.va.api.health.vistafhirquery.service.util.Translations;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class R4MedicationRequestTransformer extends R4MedicationTransformers {

  private static final Translation<String, String> VA_TYPE_TO_CATEGORY_DISPLAY =
      Translations.ofStringToString()
          .whenNullOrEmpty(returnNull())
          .whenNotFound(ignoreAndReturnNull())
          .from("I")
          .to("Inpatient")
          .from("O")
          .to("Outpatient")
          .build();

  private static final Translation<String, Status> VISTA_STATUS_TO_STATUS =
      Translations.ofStringToType(Status.class)
          .whenNullOrEmpty(returnValue(Status.unknown))
          .whenNotFound(ignoreAndReturnValue(Status.unknown))
          .from("HOLD", "PROVIDER HOLD", "ACTIVE", "SUSPENDED")
          .to(Status.active)
          .from("DRUG INTERACTIONS", "NON-VERIFIED")
          .to(Status.draft)
          .from("DISCONTINUED", "DISCONTINUED (EDIT)", "DISCONTINUED BY PROVIDER")
          .to(Status.stopped)
          .from("DELETED")
          .to(Status.entered_in_error)
          .from("EXPIRED")
          .to(Status.completed)
          .build();

  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull VprGetPatientData.Response.Results rpcResults;

  List<CodeableConcept> category(String vaType) {
    return VA_TYPE_TO_CATEGORY_DISPLAY
        .translate(vaType)
        .map(
            display ->
                CodeableConcept.builder()
                    .text(display)
                    .coding(
                        Coding.builder()
                            .code(display.toLowerCase())
                            .display(display)
                            .system(
                                "http://terminology.hl7.org/fhir/CodeSystem/medicationrequest-category")
                            .build()
                            .asList())
                    .build()
                    .asList())
        .orElse(null);
  }

  DispenseRequest dispenseRequest(
      String start, String end, String refills, String quantity, String form) {
    Period period = allBlank(start, end) ? null : Period.builder().start(start).end(end).build();
    Integer numberOfRepeatsAllowed = toInteger(refills);
    SimpleQuantity simpleQuantity = quantity(quantity, form);
    if (allBlank(period, numberOfRepeatsAllowed, simpleQuantity)) {
      return null;
    }
    return DispenseRequest.builder()
        .validityPeriod(period)
        .numberOfRepeatsAllowed(numberOfRepeatsAllowed)
        .quantity(simpleQuantity)
        .build();
  }

  Reference requester(Med.Provider orderingProvider) {
    if (isBlank(orderingProvider) || isBlank(orderingProvider.name())) {
      return null;
    }
    return toReference("Practitioner", null, orderingProvider.name());
  }

  /** Return the appropriate FHIR status for the corresponding VistA vaStatus. */
  Status status(String vistaStatus) {
    return VISTA_STATUS_TO_STATUS.translate(vistaStatus).orElse(Status.unknown);
  }

  /** Convert VistA Med data to R4 Fhir data. */
  public Stream<MedicationRequest> toFhir() {
    return rpcResults().medStream().filter(Med::isNotEmpty).map(this::toMedicationRequest);
  }

  /** required fields to be updated with correct logic later. * */
  private MedicationRequest toMedicationRequest(Med rpcMed) {
    return MedicationRequest.builder()
        .id(medicationRequestIdFrom(rpcMed.id().value(), patientIcn, site))
        .meta(Meta.builder().source(site()).build())
        .subject(toReference("Patient", patientIcn, null))
        .intent(Intent.order)
        .authoredOn(optionalInstantToString(toHumanDateTime(rpcMed.ordered())))
        .status(status(valueOfValueOnlyXmlAttribute(rpcMed.vaStatus())))
        .category(category(valueOfValueOnlyXmlAttribute(rpcMed.vaType())))
        .medicationCodeableConcept(
            Safe.stream(rpcMed.product())
                .findFirst()
                .map(R4MedicationTransformers::medicationCodeableConcept)
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
            asListOrNull(
                dosageInstruction(
                    rpcMed.sig(), valueOfValueOnlyXmlAttribute(rpcMed.ptInstructions()))))
        .build();
  }
}
