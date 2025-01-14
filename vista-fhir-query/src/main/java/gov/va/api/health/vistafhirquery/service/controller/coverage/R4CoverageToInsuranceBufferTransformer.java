package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isStringLengthInRangeInclusively;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.tryParseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.COVERAGE_CLASS_CODE_SYSTEM;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.OPTIONAL;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.CoverageClass;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.r4.api.resources.RelatedPerson;
import gov.va.api.health.vistafhirquery.service.controller.ContainedResourceReader;
import gov.va.api.health.vistafhirquery.service.controller.FilemanFactoryRegistry;
import gov.va.api.health.vistafhirquery.service.controller.FilemanIndexRegistry;
import gov.va.api.health.vistafhirquery.service.controller.IdentifierReader;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.EndDateOccursBeforeStartDate;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidContainedResource;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidStringLengthInclusively;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.BooleanExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.CodeExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.CodeableConceptExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.DateExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.lighthouse.charon.models.FilemanDate;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageToInsuranceBufferTransformer {
  static final Map<Boolean, String> YES_NO = Map.of(true, "YES", false, "NO");

  FilemanFactoryRegistry factoryRegistry;

  FilemanIndexRegistry indexRegistry;

  DateTimeFormatter vistaDateFormatter;

  ContainedResourceReader containedResourceReader;

  @NonNull Coverage coverage;

  @Builder
  R4CoverageToInsuranceBufferTransformer(@NonNull Coverage coverage, ZoneId timezone) {
    this.coverage = coverage;
    this.containedResourceReader = new ContainedResourceReader(coverage);
    this.factoryRegistry = FilemanFactoryRegistry.create();
    this.indexRegistry = FilemanIndexRegistry.create();
    this.vistaDateFormatter =
        DateTimeFormatter.ofPattern("MMddyyyy")
            .withZone(timezone == null ? ZoneId.of("UTC") : timezone);
  }

  Set<WriteableFilemanValue> address(
      String cityField,
      String stateField,
      String zipcodeField,
      List<String> lineFields,
      List<Address> addresses) {
    if (isBlank(addresses)) {
      return emptySet();
    }
    if (addresses.size() > 1) {
      throw UnexpectedNumberOfValues.builder()
          .exactExpectedCount(1)
          .receivedCount(addresses.size())
          .jsonPath(".contained[].address[]")
          .build();
    }
    Address address = addresses.get(0);
    Set<WriteableFilemanValue> addressValues = new HashSet<>();
    List<String> lines = address.line();
    if (!isBlank(lineFields) && !isBlank(lines)) {
      if (lineFields.size() < lines.size()) {
        throw UnexpectedNumberOfValues.builder()
            .exactExpectedCount(lineFields.size())
            .receivedCount(lines.size())
            .jsonPath(".contained[].address[].line[]")
            .build();
      }
      for (int i = 0; i < lines.size(); i++) {
        if (isBlank(lineFields.get(i))) {
          throw new IllegalStateException("Value of line[N] is blank at index: " + i);
        }
        factoryRegistry()
            .get(InsuranceVerificationProcessor.FILE_NUMBER)
            .forString(
                lineFields.get(i),
                indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
                lines.get(i))
            .ifPresent(addressValues::add);
      }
    }
    factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            cityField,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            address.city())
        .ifPresent(addressValues::add);
    factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            stateField,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            address.state())
        .ifPresent(addressValues::add);
    factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            zipcodeField,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            address.postalCode())
        .ifPresent(addressValues::add);
    return addressValues;
  }

  WriteableFilemanValue contactFor(List<Organization.Contact> contacts, String code, String field) {
    if (isBlank(code) || isBlank(field)) {
      return null;
    }
    var contactPointFiltered =
        Safe.stream(contacts)
            .filter(contact -> organizationContactHasCode(contact, code))
            .flatMap(c -> Safe.stream(c.telecom()))
            .filter(this::contactPointIsPhone)
            .map(ContactPoint::value)
            .filter(Objects::nonNull)
            .collect(toList());
    if (contactPointFiltered.size() > 1) {
      throw UnexpectedNumberOfValues.builder()
          .receivedCount(contactPointFiltered.size())
          .exactExpectedCount(1)
          .jsonPath(".contained[].contact[]")
          .build();
    }
    var contactPointValue = contactPointFiltered.size() == 0 ? null : contactPointFiltered.get(0);
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            field,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            contactPointValue)
        .orElse(null);
  }

  private boolean contactPointIsPhone(ContactPoint contactPoint) {
    if (isBlank(contactPoint) || isBlank(contactPoint.system())) {
      return false;
    }
    return ContactPointSystem.phone.name().equals(contactPoint.system().name());
  }

  private List<ExtensionHandler> coverageExtensionHandlers() {
    return List.of(
        DateExtensionHandler.builder()
            .definition(InsuranceBufferDefinitions.get().serviceDate())
            .tz(vistaDateFormatter.getZone())
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .index(indexRegistry.get(InsuranceVerificationProcessor.FILE_NUMBER))
            .build());
  }

  List<WriteableFilemanValue> coverageExtensions(List<Extension> extensions) {
    var extensionProcessor = R4ExtensionProcessor.of(".extension[]", coverageExtensionHandlers());
    var serviceDateDefinition = InsuranceBufferDefinitions.get().serviceDate();
    if (!serviceDateDefinition.isInCollection(extensions)) {
      return extensionProcessor.process(
          Stream.concat(Stream.of(defaultServiceDateExtension()), Safe.stream(extensions))
              .toList());
    }
    return extensionProcessor.process(extensions);
  }

  private Extension defaultServiceDateExtension() {
    return Extension.builder()
        .url(InsuranceBufferDefinitions.get().serviceDate().structureDefinition())
        .valueDate(
            DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now(ZoneId.of("UTC"))))
        .build();
  }

  private WriteableFilemanValue dependent(String dependent) {
    return factoryRegistry
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.PHARMACY_PERSON_CODE,
            indexRegistry.get(InsuranceVerificationProcessor.FILE_NUMBER),
            dependent)
        .orElse(null);
  }

  List<WriteableFilemanValue> effectiveAndExpirationDate(Period period) {
    if (isBlank(period)) {
      throw MissingRequiredField.builder().jsonPath(".period").build();
    }
    List<WriteableFilemanValue> dates = new ArrayList<>(2);
    var startInstant = tryParseDateTime(period.start());
    startInstant
        .map(this::parseFilemanDateIgnoringTime)
        .flatMap(
            start ->
                factoryRegistry()
                    .get(InsuranceVerificationProcessor.FILE_NUMBER)
                    .forString(
                        InsuranceVerificationProcessor.EFFECTIVE_DATE,
                        indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
                        start))
        .ifPresentOrElse(
            dates::add,
            () -> {
              throw UnexpectedValueForField.builder()
                  .jsonPath(".period.start")
                  .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                  .valueReceived(period.start())
                  .build();
            });
    if (isBlank(period.end())) {
      return dates;
    }
    var endDateTime =
        tryParseDateTime(period.end())
            .orElseThrow(
                () ->
                    UnexpectedValueForField.builder()
                        .jsonPath(".period.end")
                        .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                        .valueReceived(period.end())
                        .build());
    if (startInstant.get().isAfter(endDateTime)) {
      throw EndDateOccursBeforeStartDate.builder().jsonPath(".period").build();
    }
    var endDate = parseFilemanDateIgnoringTime(endDateTime);
    factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.EXPIRATION_DATE,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            endDate)
        .ifPresentOrElse(
            dates::add,
            () -> {
              throw UnexpectedValueForField.builder()
                  .jsonPath(".period.end")
                  .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                  .valueReceived(period.end())
                  .build();
            });
    return dates;
  }

  WriteableFilemanValue inqServiceTypeCode1(CodeableConcept type) {
    if (isBlank(type) || isBlank(type.coding())) {
      throw MissingRequiredField.builder().jsonPath(".type.coding[]").build();
    }
    var serviceTypeCoding =
        type.coding().stream()
            .filter(
                coding ->
                    InsuranceBufferDefinitions.get()
                        .inqServiceTypeCode()
                        .valueSet()
                        .equals(coding.system()))
            .collect(toList());
    if (serviceTypeCoding.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .identifyingFieldJsonPath(".type.coding[].system")
          .identifyingFieldValue(InsuranceBufferDefinitions.get().inqServiceTypeCode().valueSet())
          .jsonPath(".type.coding[]")
          .exactExpectedCount(1)
          .receivedCount(serviceTypeCoding.size())
          .build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forCoding(
            InsuranceBufferDefinitions.get().inqServiceTypeCode().vistaField(),
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            type.coding().get(0))
        .orElseThrow(() -> MissingRequiredField.builder().jsonPath(".type.coding[].code").build());
  }

  WriteableFilemanValue insuranceCompanyName(String maybeName) {
    if (isBlank(maybeName)) {
      throw MissingRequiredField.builder().jsonPath(".name").build();
    }
    if (!isStringLengthInRangeInclusively(3, 30, maybeName)) {
      throw InvalidStringLengthInclusively.builder()
          .jsonPath(".name")
          .inclusiveMinimum(3)
          .inclusiveMaximum(30)
          .received(maybeName.length())
          .build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            maybeName)
        .get();
  }

  Set<WriteableFilemanValue> insurancePlan(List<CoverageClass> coverageClass) {
    if (isBlank(coverageClass)) {
      throw MissingRequiredField.builder().jsonPath(".class[]").build();
    }
    var filteredCoverageTypes = coverageClass.stream().filter(this::isGroupPlan).collect(toList());
    if (filteredCoverageTypes.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".class[]")
          .identifyingFieldValue(".type[].coding[].code")
          .identifyingFieldValue("group")
          .exactExpectedCount(1)
          .receivedCount(filteredCoverageTypes.size())
          .build();
    }
    String referenceId = filteredCoverageTypes.get(0).value();
    InsurancePlan containedInsurancePlan =
        containedResourceReader().find(InsurancePlan.class, referenceId);
    try {
      return ContainedInsurancePlanTransformer.builder()
          .factoryRegistry(factoryRegistry())
          .indexRegistry(indexRegistry())
          .insurancePlan(containedInsurancePlan)
          .build()
          .toInsuranceBufferFields();
    } catch (BadRequestPayload e) {
      throw InvalidContainedResource.builder()
          .resourceType(InsurancePlan.class)
          .id(referenceId)
          .cause(e)
          .build();
    }
  }

  private boolean isGroupPlan(CoverageClass c) {
    if (c.type() == null) {
      return false;
    }
    return c.type().coding().stream()
        .anyMatch(
            coding ->
                COVERAGE_CLASS_CODE_SYSTEM.equals(coding.system())
                    && "group".equals(coding.code()));
  }

  WriteableFilemanValue nameOfInsured(List<HumanName> name) {
    if (isBlank(name)) {
      throw MissingRequiredField.builder().jsonPath(".name[]").build();
    }
    if (name.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .exactExpectedCount(1)
          .receivedCount(name.size())
          .jsonPath(".contained[].name[]")
          .build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.NAME_OF_INSURED,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            name.get(0).text())
        .orElseThrow(() -> MissingRequiredField.builder().jsonPath(".name[0].text").build());
  }

  private WriteableFilemanValue order(Integer order) {
    if (isBlank(order)) {
      return null;
    }
    return factoryRegistry
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forInteger(
            InsuranceVerificationProcessor.COORDINATION_OF_BENEFITS,
            indexRegistry.get(InsuranceVerificationProcessor.FILE_NUMBER),
            order)
        .orElseThrow(
            () ->
                UnexpectedValueForField.builder()
                    .valueReceived(order)
                    .dataType("https://www.hl7.org/fhir/datatypes.html#positiveInt")
                    .build());
  }

  List<WriteableFilemanValue> organization() {
    if (isBlank(coverage().payor())) {
      throw MissingRequiredField.builder().jsonPath(".payor[]").build();
    }
    var payor = coverage().payor();
    if (payor.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".payor")
          .identifyingFieldValue(".reference")
          .exactExpectedCount(1)
          .receivedCount(payor.size())
          .build();
    }
    String referenceId = payor.get(0).reference();
    Organization containedOrganization =
        containedResourceReader.find(Organization.class, referenceId);
    var addressLineFields =
        List.of(
            InsuranceVerificationProcessor.STREET_ADDRESS_LINE_1,
            InsuranceVerificationProcessor.STREET_ADDRESS_LINE_2,
            InsuranceVerificationProcessor.STREET_ADDRESS_LINE_3);
    var address =
        address(
            InsuranceVerificationProcessor.CITY,
            InsuranceVerificationProcessor.STATE,
            InsuranceVerificationProcessor.ZIP_CODE,
            addressLineFields,
            containedOrganization.address());
    var organizationExtensionProcessor =
        R4ExtensionProcessor.of(".extension[]", organizationExtensionHandlers());
    try {
      organizationStatus(containedOrganization.active());
      return Stream.concat(
              Stream.of(
                  insuranceCompanyName(containedOrganization.name()),
                  phoneNumber(
                      containedOrganization.telecom(), InsuranceVerificationProcessor.PHONE_NUMBER),
                  contactFor(
                      containedOrganization.contact(),
                      "BILL",
                      InsuranceVerificationProcessor.BILLING_PHONE_NUMBER),
                  contactFor(
                      containedOrganization.contact(),
                      "PRECERT",
                      InsuranceVerificationProcessor.PRECERTIFICATION_PHONE_NUMBER)),
              Stream.of(
                      address,
                      organizationExtensionProcessor.process(containedOrganization.extension()))
                  .flatMap(Collection::stream))
          .filter(Objects::nonNull)
          .toList();
    } catch (BadRequestPayload e) {
      throw InvalidContainedResource.builder()
          .resourceType(Organization.class)
          .id(referenceId)
          .cause(e)
          .build();
    }
  }

  private boolean organizationContactHasCode(Organization.Contact contact, String code) {
    if (isBlank(contact) || isBlank(contact.purpose())) {
      return false;
    }
    return Safe.stream(contact.purpose().coding()).anyMatch(p -> code.equals(p.code()));
  }

  private List<ExtensionHandler> organizationExtensionHandlers() {
    return List.of(
        CodeableConceptExtensionHandler.forDefinition(InsuranceBufferDefinitions.get().reimburse())
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .build());
  }

  void organizationStatus(Boolean active) {
    if (!Boolean.TRUE.equals(active)) {
      throw UnexpectedValueForField.builder()
          .supportedValues(List.of("true"))
          .valueReceived(active)
          .jsonPath("contained[].active")
          .dataType("https://www.hl7.org/fhir/datatypes.html#boolean")
          .build();
    }
  }

  private String parseFilemanDateIgnoringTime(Instant instant) {
    return FilemanDate.from(instant.truncatedTo(ChronoUnit.DAYS))
        .formatAsDateTime(ZoneId.of("UTC"));
  }

  WriteableFilemanValue patientId(Reference beneficiary) {
    if (isBlank(beneficiary) || isBlank(beneficiary.identifier())) {
      throw MissingRequiredField.builder().jsonPath(".beneficiary.identifier").build();
    }
    var isMemberId =
        Optional.ofNullable(beneficiary.identifier().type()).map(CodeableConcept::coding).stream()
            .flatMap(Collection::stream)
            .anyMatch(
                coding ->
                    "MB".equals(coding.code())
                        && "http://terminology.hl7.org/CodeSystem/v2-0203".equals(coding.system()));
    if (!isMemberId) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".beneficiary.identifier")
          .identifyingFieldJsonPath(".type.coding[]")
          .identifyingFieldValue(
              ".system=http://terminology.hl7.org/CodeSystem/v2-0203 and .code=MB")
          .exactExpectedCount(1)
          .receivedCount(0)
          .build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forIdentifier(
            InsuranceVerificationProcessor.PATIENT_ID,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            beneficiary.identifier())
        .orElseThrow(
            () -> MissingRequiredField.builder().jsonPath(".beneficiary.identifier.value").build());
  }

  WriteableFilemanValue patientRelationshipHipaa(CodeableConcept relationship) {
    if (isBlank(relationship) || isBlank(relationship.coding())) {
      throw MissingRequiredField.builder().jsonPath(".relationship.coding[]").build();
    }
    var relationships =
        relationship.coding().stream()
            .filter(c -> SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM.equals(c.system()))
            .toList();
    if (relationships.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".relationship")
          .identifyingFieldJsonPath(".coding[].system")
          .identifyingFieldValue(SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM)
          .exactExpectedCount(1)
          .receivedCount(relationships.size())
          .build();
    }
    var relationshipCode = relationships.get(0);
    return SubscriberToBeneficiaryRelationship.fromCoding(relationshipCode)
        .map(
            factoryRegistry()
                .get(InsuranceVerificationProcessor.FILE_NUMBER)
                .toString(
                    InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA,
                    index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER)),
                    SubscriberToBeneficiaryRelationship::display))
        .orElseThrow(
            () ->
                UnexpectedValueForField.builder()
                    .jsonPath(".relationship.coding[]")
                    .valueSet(SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM)
                    .valueReceived(relationshipCode)
                    .build());
  }

  WriteableFilemanValue phoneNumber(List<ContactPoint> telecom, String phoneField) {
    var contactPointFiltered =
        Safe.stream(telecom)
            .filter(this::contactPointIsPhone)
            .map(ContactPoint::value)
            .collect(toList());
    if (contactPointFiltered.size() > 1) {
      throw UnexpectedNumberOfValues.builder()
          .exactExpectedCount(1)
          .receivedCount(contactPointFiltered.size())
          .jsonPath(".contained[].identifiers[]")
          .build();
    }
    var contactPointValue = contactPointFiltered.size() == 0 ? null : contactPointFiltered.get(0);
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            phoneField,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            contactPointValue)
        .orElse(null);
  }

  private List<WriteableFilemanValue> relatedPerson() {
    if (isBlank(coverage().subscriber())) {
      return emptyList();
    }
    String referenceId = coverage().subscriber().reference();
    RelatedPerson containedRelatedPerson =
        containedResourceReader.find(RelatedPerson.class, referenceId);
    var reader =
        IdentifierReader.forDefinitions(List.of(InsuranceBufferDefinitions.get().insuredsSsn()))
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .indexRegistry(indexRegistry())
            .build();
    var relatedPersonExtensionProcessor =
        R4ExtensionProcessor.of(".extension[]", relatedPersonExtensionHandlers());
    try {
      return Stream.concat(
              Stream.of(
                  relatedPersonBirthDate(containedRelatedPerson.birthDate()),
                  phoneNumber(
                      containedRelatedPerson.telecom(),
                      InsuranceVerificationProcessor.SUBSCRIBER_PHONE),
                  nameOfInsured(containedRelatedPerson.name())),
              Stream.of(
                      relatedPersonAddress(containedRelatedPerson.address()),
                      unknownToEmpty(
                          relatedPersonExtensionProcessor.process(
                              containedRelatedPerson.extension())),
                      reader.process(containedRelatedPerson.identifier()))
                  .flatMap(Collection::stream))
          .filter(Objects::nonNull)
          .toList();
    } catch (BadRequestPayload e) {
      throw InvalidContainedResource.builder()
          .resourceType(RelatedPerson.class)
          .id(referenceId)
          .cause(e)
          .build();
    }
  }

  Set<WriteableFilemanValue> relatedPersonAddress(List<Address> addresses) {
    if (isBlank(addresses)) {
      return emptySet();
    }
    if (addresses.size() > 1) {
      throw UnexpectedNumberOfValues.builder()
          .exactExpectedCount(1)
          .receivedCount(addresses.size())
          .jsonPath(".contained[].address[]")
          .build();
    }
    var addressLineFields =
        List.of(
            InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_1,
            InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_2);
    Address address = addresses.get(0);
    Set<WriteableFilemanValue> addressValues = new HashSet<>();
    addressValues.addAll(
        address(
            InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_CITY,
            InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_STATE,
            InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_ZIP,
            addressLineFields,
            addresses));
    factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_COUNTRY,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            address.country())
        .ifPresent(addressValues::add);
    factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_SUBDIVISION,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            address.district())
        .ifPresent(addressValues::add);
    return addressValues;
  }

  WriteableFilemanValue relatedPersonBirthDate(String birthDate) {
    if (isBlank(birthDate)) {
      throw MissingRequiredField.builder().jsonPath(".contained[].birthDate").build();
    }
    String vistaBirthDate =
        tryParseDateTime(birthDate)
            .map(this::parseFilemanDateIgnoringTime)
            .orElseThrow(
                () ->
                    UnexpectedValueForField.builder()
                        .jsonPath(".contained[].birthDate")
                        .dataType("http://hl7.org/fhir/R4/datatypes.html#date")
                        .valueReceived(birthDate)
                        .build());
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.INSUREDS_DOB,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            vistaBirthDate)
        .get();
  }

  private List<ExtensionHandler> relatedPersonExtensionHandlers() {
    return List.of(
        CodeExtensionHandler.builder()
            .definition(InsuranceBufferDefinitions.get().insuredsSex())
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .build());
  }

  WriteableFilemanValue subscriberId(String subscriberId) {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.SUBSCRIBER_ID,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            subscriberId)
        .orElseThrow(() -> MissingRequiredField.builder().jsonPath(".subscriberId").build());
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceBuffer() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    // Contained resources have required fields in them
    if (isBlank(coverage().contained())) {
      throw MissingRequiredField.builder().jsonPath(".contained[]").build();
    }
    fields.addAll(coverageExtensions(coverage().extension()));
    fields.add(patientId(coverage().beneficiary()));
    fields.add(dependent(coverage().dependent()));
    fields.add(order(coverage().order()));
    fields.add(patientRelationshipHipaa(coverage().relationship()));
    fields.add(subscriberId(coverage().subscriberId()));
    fields.add(inqServiceTypeCode1(coverage().type()));
    fields.addAll(effectiveAndExpirationDate(coverage().period()));
    fields.addAll(insurancePlan(coverage().coverageClass()));
    fields.addAll(organization());
    fields.addAll(relatedPerson());
    return fields.stream().filter(Objects::nonNull).collect(Collectors.toSet());
  }

  private List<WriteableFilemanValue> unknownToEmpty(List<WriteableFilemanValue> extensions) {
    return extensions.stream().filter(s -> !"EMPTY".equals(s.value())).toList();
  }

  @Value
  @Builder
  public static class ContainedInsurancePlanTransformer {
    @NonNull InsurancePlan insurancePlan;

    @NonNull FilemanFactoryRegistry factoryRegistry;

    @NonNull FilemanIndexRegistry indexRegistry;

    private List<ExtensionHandler> extensionHandlers() {
      return List.of(
          BooleanExtensionHandler.forDefiningUrl(
                  InsuranceBufferStructureDefinitions.UTILIZATION_REVIEW_REQUIRED)
              .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .fieldNumber(InsuranceVerificationProcessor.UTILIZATION_REVIEW_REQUIRED)
              .index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .required(OPTIONAL)
              .booleanStringMapping(YES_NO)
              .build(),
          BooleanExtensionHandler.forDefiningUrl(
                  InsuranceBufferStructureDefinitions.PRECERTIFICATION_REQUIRED)
              .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .fieldNumber(InsuranceVerificationProcessor.PRECERTIFICATION_REQUIRED)
              .index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .required(OPTIONAL)
              .booleanStringMapping(YES_NO)
              .build(),
          BooleanExtensionHandler.forDefiningUrl(
                  InsuranceBufferStructureDefinitions.AMBULATORY_CARE_CERTIFICATION)
              .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .fieldNumber(InsuranceVerificationProcessor.AMBULATORY_CARE_CERTIFICATION)
              .index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .required(OPTIONAL)
              .booleanStringMapping(YES_NO)
              .build(),
          BooleanExtensionHandler.forDefiningUrl(
                  InsuranceBufferStructureDefinitions.EXCLUDE_PREEXISTING_CONDITION)
              .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .fieldNumber(InsuranceVerificationProcessor.EXCLUDE_PREEXISTING_CONDITION)
              .index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .required(OPTIONAL)
              .booleanStringMapping(YES_NO)
              .build(),
          BooleanExtensionHandler.forDefiningUrl(
                  InsuranceBufferStructureDefinitions.BENEFITS_ASSIGNABLE)
              .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .fieldNumber(InsuranceVerificationProcessor.BENEFITS_ASSIGNABLE)
              .index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
              .required(OPTIONAL)
              .booleanStringMapping(YES_NO)
              .build());
    }

    Optional<WriteableFilemanValue> groupName(String name) {
      if (isBlank(name)) {
        return Optional.empty();
      }
      if (!isStringLengthInRangeInclusively(2, 20, name)) {
        throw InvalidStringLengthInclusively.builder()
            .jsonPath(".name")
            .inclusiveMinimum(2)
            .inclusiveMaximum(20)
            .received(name.length())
            .build();
      }
      return factoryRegistry()
          .get(InsuranceVerificationProcessor.FILE_NUMBER)
          .forString(
              InsuranceVerificationProcessor.GROUP_NAME,
              indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
              name);
    }

    private IdentifierReader identifierReader() {
      // GroupNumber is required
      if (isBlank(insurancePlan().identifier())) {
        throw MissingRequiredField.builder().jsonPath(".identifier[]").build();
      }
      return IdentifierReader.forDefinitions(
              List.of(
                  InsuranceBufferDefinitions.get().groupNumber(),
                  InsuranceBufferDefinitions.get().bankingIdentificationNumber(),
                  InsuranceBufferDefinitions.get().processorControlNumber()))
          .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
          .indexRegistry(indexRegistry())
          .build();
    }

    /** Transform InsurancePlan resource to InsuranceVerificationProcessor fields. */
    public Set<WriteableFilemanValue> toInsuranceBufferFields() {
      Set<WriteableFilemanValue> filemanValues = new HashSet<>();
      filemanValues.addAll(identifierReader().process(insurancePlan().identifier()));
      filemanValues.addAll(
          R4ExtensionProcessor.of(".extension[]", extensionHandlers())
              .process(insurancePlan().extension()));
      groupName(insurancePlan().name()).ifPresent(filemanValues::add);
      typeOfPlan(insurancePlan().plan()).ifPresent(filemanValues::add);
      return filemanValues;
    }

    Optional<WriteableFilemanValue> typeOfPlan(List<InsurancePlan.Plan> plan) {
      if (isBlank(plan)) {
        return Optional.empty();
      }
      if (plan.size() != 1) {
        throw UnexpectedNumberOfValues.builder()
            .jsonPath(".plan[]")
            .exactExpectedCount(1)
            .receivedCount(plan.size())
            .build();
      }
      var type = plan.get(0).type();
      if (isBlank(type) || isBlank(type.coding())) {
        return Optional.empty();
      }
      var definition = InsuranceBufferDefinitions.get().typeOfPlan();
      var planTypes =
          type.coding().stream()
              .filter(t -> definition.valueSet().equals(t.system()))
              .map(Coding::display)
              .toList();
      if (planTypes.size() != 1) {
        throw UnexpectedNumberOfValues.builder()
            .jsonPath(".plan[0].type.coding[]")
            .exactExpectedCount(1)
            .receivedCount(planTypes.size())
            .identifyingFieldJsonPath(".system")
            .identifyingFieldValue(definition.valueSet())
            .build();
      }
      return factoryRegistry()
          .get(InsuranceVerificationProcessor.FILE_NUMBER)
          .forString(
              definition.vistaField(),
              indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
              planTypes.get(0));
    }
  }
}
