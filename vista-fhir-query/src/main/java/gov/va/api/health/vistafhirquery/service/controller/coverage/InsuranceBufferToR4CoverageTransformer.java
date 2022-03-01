package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.emptyToNull;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static java.util.Collections.emptyList;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.RelatedPerson;
import gov.va.api.health.vistafhirquery.service.controller.ContainedResourceWriter;
import gov.va.api.health.vistafhirquery.service.controller.ContainedResourceWriter.ContainableResource;
import gov.va.api.health.vistafhirquery.service.controller.ExtensionFactory;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformers;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InsuranceBufferToR4CoverageTransformer {
  public static final List<String> MAPPED_VISTA_FIELDS =
      List.of(
          InsuranceVerificationProcessor.AMBULATORY_CARE_CERTIFICATION,
          InsuranceVerificationProcessor.BANKING_IDENTIFICATION_NUMBER,
          InsuranceVerificationProcessor.BENEFITS_ASSIGNABLE,
          InsuranceVerificationProcessor.BILLING_PHONE_NUMBER,
          InsuranceVerificationProcessor.CITY,
          InsuranceVerificationProcessor.EXCLUDE_PREEXISTING_CONDITION,
          InsuranceVerificationProcessor.EFFECTIVE_DATE,
          InsuranceVerificationProcessor.EXPIRATION_DATE,
          InsuranceVerificationProcessor.GROUP_NAME,
          InsuranceVerificationProcessor.GROUP_NUMBER,
          InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1,
          InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME,
          InsuranceVerificationProcessor.INSUREDS_DOB,
          InsuranceVerificationProcessor.NAME_OF_INSURED,
          InsuranceVerificationProcessor.PATIENT_ID,
          InsuranceVerificationProcessor.PATIENT_NAME,
          InsuranceVerificationProcessor.PHONE_NUMBER,
          InsuranceVerificationProcessor.PRECERTIFICATION_PHONE_NUMBER,
          InsuranceVerificationProcessor.PRECERTIFICATION_REQUIRED,
          InsuranceVerificationProcessor.PROCESSOR_CONTROL_NUMBER_PCN,
          InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA,
          InsuranceVerificationProcessor.REIMBURSE,
          InsuranceVerificationProcessor.STATE,
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_1,
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_2,
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_3,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_CITY,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_COUNTRY,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_1,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_2,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_STATE,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_SUBDIVISION,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_ZIP,
          InsuranceVerificationProcessor.SUBSCRIBER_ID,
          InsuranceVerificationProcessor.SUBSCRIBER_PHONE,
          InsuranceVerificationProcessor.TYPE_OF_PLAN,
          InsuranceVerificationProcessor.UTILIZATION_REVIEW_REQUIRED,
          InsuranceVerificationProcessor.WHOSE_INSURANCE,
          InsuranceVerificationProcessor.ZIP_CODE);

  static final Map<String, Boolean> YES_NO = Map.of("1", true, "0", false);

  @NonNull String patientIcn;

  @NonNull String site;

  /** Assumes UTC if zoneId is not provided. */
  @Builder.Default ZoneId vistaZoneId = ZoneOffset.UTC;

  @NonNull LhsLighthouseRpcGatewayResponse.Results results;

  private Reference beneficiary(@NonNull String patientIcn, Optional<String> maybePatientId) {
    var ref = toReference(Patient.class.getSimpleName(), patientIcn, null);
    if (ref == null) {
      throw new IllegalStateException(
          "Reference could not be created using a non-null parameter: " + patientIcn);
    }
    maybePatientId.ifPresent(
        patientId ->
            ref.identifier(
                Identifier.builder()
                    .type(
                        CodeableConcept.builder()
                            .coding(
                                Coding.builder()
                                    .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                    .code("MB")
                                    .display("Member Number")
                                    .build()
                                    .asList())
                            .build())
                    .value(patientId)
                    .build()));
    return ref;
  }

  private Organization.Contact billingContact(FilemanEntry entry) {
    var billingPhoneNumber =
        entry.internal(InsuranceVerificationProcessor.BILLING_PHONE_NUMBER).map(this::phone);
    if (isBlank(billingPhoneNumber)) {
      return null;
    }
    return Organization.Contact.builder()
        .telecom(billingPhoneNumber.get().asList())
        .purpose(
            asCodeableConcept(
                Coding.builder()
                    .code("BILL")
                    .display("BILL")
                    .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                    .build()))
        .build();
  }

  private List<Organization.Contact> contacts(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return emptyToNull(
        Stream.of(billingContact(entry), precertificationContact(entry))
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
  }

  private ContainedResourceWriter<Coverage> contained(FilemanEntry entry) {
    return ContainedResourceWriter.of(
        List.of(
            ContainableResource.<Coverage, InsurancePlan>builder()
                .containedResource(toInsurancePlan(entry))
                .applyReferenceId((c, id) -> c.coverageClass(coverageClass(id)))
                .build(),
            ContainableResource.<Coverage, Organization>builder()
                .containedResource(toOrganization(entry))
                .applyReferenceId(
                    (o, id) ->
                        o.payor(
                            Reference.builder()
                                .type(Organization.class.getSimpleName())
                                .reference(id)
                                .build()
                                .asList()))
                .build(),
            ContainableResource.<Coverage, RelatedPerson>builder()
                .containedResource(toRelatedPerson(entry))
                .applyReferenceId(
                    (c, id) ->
                        c.subscriber(
                            Reference.builder()
                                .type(RelatedPerson.class.getSimpleName())
                                .reference(id)
                                .build()))
                .build()));
  }

  private List<Coverage.CoverageClass> coverageClass(String referenceId) {
    return Coverage.CoverageClass.builder()
        .type(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/coverage-class")
                            .code("group")
                            .build()))
                .build())
        .value(referenceId)
        .build()
        .asList();
  }

  private List<Extension> extensions(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    ExtensionFactory extensions = ExtensionFactory.of(entry, YES_NO);
    return emptyToNull(
        Stream.of(
                extensions.ofCodeableConceptFromExternalValue(
                    InsuranceVerificationProcessor.REIMBURSE,
                    InsuranceBufferStructureDefinitions.REIMBURSE_URN_OID,
                    InsuranceBufferStructureDefinitions.REIMBURSE))
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
  }

  private List<Extension> insurancePlanExtensions(FilemanEntry entry) {
    ExtensionFactory extensions = ExtensionFactory.of(entry, YES_NO);
    return Stream.of(
            extensions.ofYesNoBoolean(
                InsuranceVerificationProcessor.UTILIZATION_REVIEW_REQUIRED,
                InsuranceBufferStructureDefinitions.UTILIZATION_REVIEW_REQUIRED),
            extensions.ofYesNoBoolean(
                InsuranceVerificationProcessor.PRECERTIFICATION_REQUIRED,
                InsuranceBufferStructureDefinitions.PRECERTIFICATION_REQUIRED),
            extensions.ofYesNoBoolean(
                InsuranceVerificationProcessor.AMBULATORY_CARE_CERTIFICATION,
                InsuranceBufferStructureDefinitions.AMBULATORY_CARE_CERTIFICATION),
            extensions.ofYesNoBoolean(
                InsuranceVerificationProcessor.EXCLUDE_PREEXISTING_CONDITION,
                InsuranceBufferStructureDefinitions.EXCLUDE_PREEXISTING_CONDITION),
            extensions.ofYesNoBoolean(
                InsuranceVerificationProcessor.BENEFITS_ASSIGNABLE,
                InsuranceBufferStructureDefinitions.BENEFITS_ASSIGNABLE))
        .filter(Objects::nonNull)
        .toList();
  }

  private Identifier insurancePlanIdentifier(
      FilemanEntry entry, String fieldNumber, String system) {
    return entry
        .internal(fieldNumber)
        .map(num -> Identifier.builder().system(system).value(num).build())
        .orElse(null);
  }

  private List<Identifier> insurancePlanIdentifiers(FilemanEntry entry) {
    return Stream.of(
            insurancePlanIdentifier(
                entry,
                InsuranceVerificationProcessor.GROUP_NUMBER,
                InsuranceBufferStructureDefinitions.GROUP_NUMBER),
            insurancePlanIdentifier(
                entry,
                InsuranceVerificationProcessor.BANKING_IDENTIFICATION_NUMBER,
                InsuranceBufferStructureDefinitions.BANKING_IDENTIFICATION_NUMBER),
            insurancePlanIdentifier(
                entry,
                InsuranceVerificationProcessor.PROCESSOR_CONTROL_NUMBER_PCN,
                InsuranceBufferStructureDefinitions.PROCESSOR_CONTROL_NUMBER_PCN))
        .filter(Objects::nonNull)
        .toList();
  }

  private List<InsurancePlan.Plan> insurancePlanType(FilemanEntry entry) {
    CodeableConcept typeOfPlan =
        type(
            entry,
            InsuranceVerificationProcessor.TYPE_OF_PLAN,
            InsuranceBufferStructureDefinitions.TYPE_OF_PLAN);
    if (typeOfPlan == null) {
      return emptyList();
    }
    return InsurancePlan.Plan.builder().type(typeOfPlan).build().asList();
  }

  private List<Address> payorAddress(FilemanEntry entry) {
    String streetAddressLine1 =
        entry.internal(InsuranceVerificationProcessor.STREET_ADDRESS_LINE_1).orElse(null);
    String streetAddressLine2 =
        entry.internal(InsuranceVerificationProcessor.STREET_ADDRESS_LINE_2).orElse(null);
    String streetAddressLine3 =
        entry.internal(InsuranceVerificationProcessor.STREET_ADDRESS_LINE_3).orElse(null);
    String city = entry.internal(InsuranceVerificationProcessor.CITY).orElse(null);
    String state = entry.external(InsuranceVerificationProcessor.STATE).orElse(null);
    String zipCode = entry.internal(InsuranceVerificationProcessor.ZIP_CODE).orElse(null);
    if (allBlank(
        streetAddressLine1, streetAddressLine2, streetAddressLine3, city, state, zipCode)) {
      return null;
    }
    return Address.builder()
        .city(city)
        .state(state)
        .line(
            emptyToNull(
                Stream.of(streetAddressLine1, streetAddressLine2, streetAddressLine3)
                    .filter(Objects::nonNull)
                    .toList()))
        .postalCode(zipCode)
        .build()
        .asList();
  }

  private Period period(FilemanEntry entry) {
    Period period = Period.builder().build();
    entry
        .internal(InsuranceVerificationProcessor.EFFECTIVE_DATE, this::toFilemanDate)
        .ifPresent(period::start);
    entry
        .internal(InsuranceVerificationProcessor.EXPIRATION_DATE, this::toFilemanDate)
        .ifPresent(period::end);
    if (allBlank(period.start(), period.end())) {
      return null;
    }
    return period;
  }

  private ContactPoint phone(String maybePhone) {
    if (isBlank(maybePhone)) {
      return null;
    }
    return ContactPoint.builder()
        .value(maybePhone)
        .system(ContactPoint.ContactPointSystem.phone)
        .build();
  }

  private Organization.Contact precertificationContact(FilemanEntry entry) {
    var precertificationPhoneNumber =
        entry
            .internal(InsuranceVerificationProcessor.PRECERTIFICATION_PHONE_NUMBER)
            .map(this::phone);
    if (isBlank(precertificationPhoneNumber)) {
      return null;
    }
    return Organization.Contact.builder()
        .telecom(precertificationPhoneNumber.get().asList())
        .purpose(
            asCodeableConcept(
                Coding.builder()
                    .code("PRECERT")
                    .display("PRECERT")
                    .system("https://va.gov/fhir/CodeSystem/organization-contactType")
                    .build()))
        .build();
  }

  private CodeableConcept relationship(String ptRelationshipHipaa) {
    if (isBlank(ptRelationshipHipaa)) {
      return null;
    }
    return SubscriberToBeneficiaryRelationship.forCode(ptRelationshipHipaa)
        .map(
            relationship ->
                CodeableConcept.builder().coding(relationship.asCoding().asList()).build())
        .orElse(null);
  }

  private List<Address> subscriberAddress(FilemanEntry entry) {
    String streetAddressLine1 =
        entry.internal(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_1).orElse(null);
    String streetAddressLine2 =
        entry.internal(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_2).orElse(null);
    String city =
        entry.internal(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_CITY).orElse(null);
    String state =
        entry.external(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_STATE).orElse(null);
    String country =
        entry.external(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_COUNTRY).orElse(null);
    String subdivision =
        entry.internal(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_SUBDIVISION).orElse(null);
    String zipCode =
        entry.internal(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_ZIP).orElse(null);
    if (allBlank(
        streetAddressLine1, streetAddressLine2, city, subdivision, state, country, zipCode)) {
      return null;
    }
    return Address.builder()
        .city(city)
        .state(state)
        .line(
            emptyToNull(
                Stream.of(streetAddressLine1, streetAddressLine2)
                    .filter(Objects::nonNull)
                    .toList()))
        .postalCode(zipCode)
        .country(country)
        .district(subdivision)
        .build()
        .asList();
  }

  private String subscriberBirthDate(FilemanEntry entry) {
    return entry
        .internal(InsuranceVerificationProcessor.INSUREDS_DOB)
        .flatMap(R4Transformers::toHumanDateTime)
        .map(hdt -> hdt.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE))
        .orElse(null);
  }

  private List<Extension> subscriberExtensions(FilemanEntry entry) {
    ExtensionFactory extensions = ExtensionFactory.of(entry, YES_NO);
    return emptyToNull(
        Stream.of(
                extensions.ofCodeableConceptFromInternalValue(
                    InsuranceVerificationProcessor.INSUREDS_SEX,
                    InsuranceBufferStructureDefinitions.INSUREDS_SEX_SYSTEM,
                    InsuranceBufferStructureDefinitions.INSUREDS_SEX_URL))
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
  }

  private List<Identifier> subscriberIdentifier(FilemanEntry entry) {
    return entry
        .internal(InsuranceVerificationProcessor.INSUREDS_SSN)
        .map(
            ssn ->
                Identifier.builder()
                    .use(Identifier.IdentifierUse.official)
                    .type(
                        CodeableConcept.builder()
                            .coding(
                                Coding.builder()
                                    .system("http://hl7.org/fhir/v2/0203")
                                    .code("SB")
                                    .build()
                                    .asList())
                            .build())
                    .system("http://hl7.org/fhir/sid/us-ssn")
                    .value(ssn)
                    .assigner(
                        Reference.builder().display("United States Social Security Number").build())
                    .build()
                    .asList())
        .orElse(null);
  }

  private Coverage toCoverage(FilemanEntry entry) {
    if (entry == null || isBlank(entry.fields())) {
      return null;
    }
    var coverage =
        Coverage.builder()
            .id(
                PatientTypeCoordinates.builder()
                    .icn(patientIcn())
                    .site(site())
                    .file(entry.file())
                    .ien(entry.ien())
                    .build()
                    .toString())
            .period(period(entry))
            .meta(Meta.builder().source(site()).build())
            .status(Status.draft)
            .type(
                entry
                    .internal(InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1)
                    .map(this::type)
                    .orElse(null))
            .subscriberId(entry.internal(InsuranceVerificationProcessor.SUBSCRIBER_ID).orElse(null))
            .beneficiary(
                beneficiary(
                    patientIcn(), entry.internal(InsuranceVerificationProcessor.PATIENT_ID)))
            .relationship(
                entry
                    .internal(InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA)
                    .map(this::relationship)
                    .orElse(null))
            .build();
    contained(entry).addContainedResources(coverage);
    return coverage;
  }

  public Stream<Coverage> toFhir() {
    return Safe.stream(results.results()).map(this::toCoverage).filter(Objects::nonNull);
  }

  private String toFilemanDate(String filemanDate) {
    return toHumanDateTime(filemanDate, vistaZoneId())
        .map(t -> t.atZone(ZoneOffset.UTC))
        .map(t -> t.format(DateTimeFormatter.ISO_DATE_TIME))
        .orElse(null);
  }

  private InsurancePlan toInsurancePlan(FilemanEntry entry) {
    var ip =
        InsurancePlan.builder()
            .extension(insurancePlanExtensions(entry))
            .identifier(insurancePlanIdentifiers(entry))
            .name(entry.internal(InsuranceVerificationProcessor.GROUP_NAME).orElse(null))
            .plan(insurancePlanType(entry))
            .build();
    if (InsurancePlan.builder().build().equals(ip)) {
      return null;
    }
    return ip;
  }

  private Organization toOrganization(FilemanEntry entry) {
    var org =
        Organization.builder()
            .active(true)
            .address(payorAddress(entry))
            .contact(contacts(entry))
            .extension(extensions(entry))
            .name(
                entry.internal(InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME).orElse(null))
            .telecom(
                entry
                    .internal(InsuranceVerificationProcessor.PHONE_NUMBER)
                    .map(this::phone)
                    .map(ContactPoint::asList)
                    .orElse(null))
            .build();
    if (Organization.builder().active(true).build().equals(org)) {
      return null;
    }
    return org;
  }

  private RelatedPerson toRelatedPerson(FilemanEntry entry) {
    var rp =
        RelatedPerson.builder()
            .birthDate(subscriberBirthDate(entry))
            .extension(subscriberExtensions(entry))
            .identifier(subscriberIdentifier(entry))
            .address(subscriberAddress(entry))
            .telecom(
                entry
                    .internal(InsuranceVerificationProcessor.SUBSCRIBER_PHONE)
                    .map(this::phone)
                    .map(ContactPoint::asList)
                    .orElse(null))
            .build();
    if (RelatedPerson.builder().build().equals(rp)) {
      return null;
    }
    return rp;
  }

  private CodeableConcept type(FilemanEntry entry, String fieldName, String system) {
    Optional<String> code = entry.internal(fieldName);
    Optional<String> display = entry.external(fieldName);
    if (isBlank(code)) {
      return null;
    }
    return asCodeableConcept(
            Coding.builder().code(code.get()).display(display.orElse(null)).system(system).build())
        .text(display.orElse(null));
  }

  private CodeableConcept type(String inqServiceTypeCode) {
    if (isBlank(inqServiceTypeCode)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            Coding.builder()
                .system(InsuranceBufferStructureDefinitions.INQ_SERVICE_TYPE_CODE)
                .code(inqServiceTypeCode)
                .build()
                .asList())
        .build();
  }
}
