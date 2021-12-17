package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.OPTIONAL;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static java.util.Collections.emptySet;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.r4.api.resources.Organization.Contact;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredListItem;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.BooleanExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.CodeableConceptExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionProcessor;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.QuantityExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ReferenceExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.StringExtensionHandler;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.N277EdiIdNumber;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@SuppressWarnings("EnhancedSwitchMigration")
@Value
public class R4OrganizationToInsuranceCompanyFileTransformer {
  static final Map<Boolean, String> YES_NO = Map.of(true, "YES", false, "NO");

  private static final WriteableFilemanValueFactory filemanFactory =
      WriteableFilemanValueFactory.forFile(InsuranceCompany.FILE_NUMBER);

  @NonNull Organization organization;

  ExtensionProcessor extensionProcessor =
      R4ExtensionProcessor.of(".extension[]", extensionHandlers());

  boolean include277EdiNumber;

  @Builder
  R4OrganizationToInsuranceCompanyFileTransformer(
      @NonNull Organization organization, boolean include277EdiNumber) {
    this.organization = organization;
    this.include277EdiNumber = include277EdiNumber;
  }

  private void addRequiredFields(Set<WriteableFilemanValue> fields) {
    fields.add(
        insuranceCompanyCoordinates(InsuranceCompany.NAME, 1, organization().name())
            .orElseThrow(() -> MissingRequiredField.builder().jsonPath(".name").build()));
    fields.addAll(
        address(
            ".address",
            "organization",
            InsuranceCompany.STREET_ADDRESS_LINE_1_,
            InsuranceCompany.STREET_ADDRESS_LINE_2_,
            InsuranceCompany.STREET_ADDRESS_LINE_3_,
            InsuranceCompany.CITY,
            InsuranceCompany.STATE,
            InsuranceCompany.ZIP_CODE,
            addressOrDie(organization().address())));
    fields.addAll(contacts());
    fields.add(
        contactPoint(
            organization().telecom(),
            InsuranceCompany.FAX_NUMBER,
            "organization",
            ContactPointSystem.fax));
    fields.add(
        contactPoint(
            organization().telecom(),
            InsuranceCompany.PHONE_NUMBER,
            "organization",
            ContactPointSystem.phone));
    fields.addAll(extensionProcessor.process(organization().extension()));
    fields.add(
        identifier(
                InsuranceCompany.EDI_ID_NUMBER_INST,
                OrganizationStructureDefinitions.EDI_ID_NUMBER_INST_CODE,
                true)
            .orElseThrow(
                () ->
                    MissingRequiredListItem.builder()
                        .jsonPath(".identifier[]")
                        .qualifiers(
                            List.of(
                                ".identifier[].code is"
                                    + OrganizationStructureDefinitions.EDI_ID_NUMBER_INST_CODE))
                        .build()));
    fields.add(
        identifier(
                InsuranceCompany.EDI_ID_NUMBER_PROF,
                OrganizationStructureDefinitions.EDI_ID_NUMBER_PROF_CODE,
                true)
            .orElseThrow(
                () ->
                    MissingRequiredListItem.builder()
                        .jsonPath(".identifier[]")
                        .qualifiers(
                            List.of(
                                ".identifier[].code is"
                                    + OrganizationStructureDefinitions.EDI_ID_NUMBER_PROF_CODE))
                        .build()));
    identifier(InsuranceCompany.BIN_NUMBER, OrganizationStructureDefinitions.BIN_NUMBER_CODE, false)
        .ifPresent(fields::add);
    identifier(
            InsuranceCompany.EDI_ID_NUMBER_DENTAL,
            OrganizationStructureDefinitions.EDI_ID_NUMBER_DENTAL_CODE,
            false)
        .ifPresent(fields::add);
    fields.addAll(
        optionalIdentifierFieldAndQualifier(
            InsuranceCompany.EDI_INST_SECONDARY_ID_1_,
            InsuranceCompany.EDI_INST_SECONDARY_ID_QUAL_1_,
            OrganizationStructureDefinitions.EDI_INST_SECONDARY_ID_QUAL_1));
    fields.addAll(
        optionalIdentifierFieldAndQualifier(
            InsuranceCompany.EDI_INST_SECONDARY_ID_2_,
            InsuranceCompany.EDI_INST_SECONDARY_ID_QUAL_2_,
            OrganizationStructureDefinitions.EDI_INST_SECONDARY_ID_QUAL_2));
    fields.addAll(
        optionalIdentifierFieldAndQualifier(
            InsuranceCompany.EDI_PROF_SECONDARY_ID_1_,
            InsuranceCompany.EDI_PROF_SECONDARY_ID_QUAL_1_,
            OrganizationStructureDefinitions.EDI_PROF_SECONDARY_ID_QUAL_1));
    fields.addAll(
        optionalIdentifierFieldAndQualifier(
            InsuranceCompany.EDI_PROF_SECONDARY_ID_2_,
            InsuranceCompany.EDI_PROF_SECONDARY_ID_QUAL_2_,
            OrganizationStructureDefinitions.EDI_PROF_SECONDARY_ID_QUAL_2));
    if (include277EdiNumber()) {
      fields.addAll(n277EdiIdentifier());
    }
  }

  Set<WriteableFilemanValue> address(
      String jsonPathBase,
      String contactPurpose,
      String streetLine1,
      String streetLine2,
      String streetLine3,
      String city,
      String state,
      String zipcode,
      Address address) {
    if (isBlank(address)) {
      return emptySet();
    }
    Set<WriteableFilemanValue> addressValues = new HashSet<>();
    List<String> lines = address.line();
    if (isBlank(lines)) {
      throw MissingRequiredField.builder().jsonPath(jsonPathBase + ".line[]").build();
    }
    if (lines.size() > 3) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(jsonPathBase + ".line[]")
          .maximumExpectedCount(3)
          .receivedCount(lines.size())
          .build();
    }
    String[] arrayLines = new String[] {streetLine1, streetLine2, streetLine3};
    for (int i = 0; i < lines.size(); i++) {
      int index = i;
      addressValues.add(
          filemanFactory
              .forString(arrayLines[i], 1, lines.get(i))
              .orElseThrow(
                  () ->
                      MissingRequiredListItem.builder()
                          .jsonPath(jsonPathBase + "line[]")
                          .qualifiers(
                              List.of(
                                  "contact purpose is " + contactPurpose, "line index = " + index))
                          .build()));
    }
    addressValues.add(
        filemanFactory
            .forString(city, 1, address.city())
            .orElseThrow(
                () ->
                    MissingRequiredListItem.builder()
                        .jsonPath(jsonPathBase + ".city")
                        .qualifiers(List.of("contact purpose is " + contactPurpose))
                        .build()));
    addressValues.add(
        filemanFactory
            .forString(state, 1, address.state())
            .orElseThrow(
                () ->
                    MissingRequiredListItem.builder()
                        .jsonPath(jsonPathBase + ".state")
                        .qualifiers(List.of("contact purpose is " + contactPurpose))
                        .build()));
    addressValues.add(
        filemanFactory
            .forString(zipcode, 1, address.postalCode())
            .orElseThrow(
                () ->
                    MissingRequiredListItem.builder()
                        .jsonPath(jsonPathBase + ".postalCode")
                        .qualifiers(List.of("contact purpose is " + contactPurpose))
                        .build()));
    return addressValues;
  }

  Address addressOrDie(List<Address> address) {
    if (address.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".address[]")
          .exactExpectedCount(1)
          .receivedCount(address.size())
          .build();
    }
    return address.get(0);
  }

  Set<WriteableFilemanValue> contact(@NonNull ContactPurpose purpose, @NonNull Contact contact) {
    switch (purpose) {
      case APPEAL:
        return contactAppeals(contact);
      case BILL:
        return contactBilling(contact);
      case DENTALCLAIMS:
        return contactDentalClaims(contact);
      case INPTCLAIMS:
        return contactInpatientClaims(contact);
      case OUTPTCLAIMS:
        return contactOutpatientClaims(contact);
      case INQUIRY:
        return contactInquiry(contact);
      case PRECERT:
        return contactPrecert(contact);
      case RXCLAIMS:
        return contactRxClaims(contact);
      case VERIFY:
        return contactVerify(contact);
      default:
        throw UnexpectedValueForField.builder()
            .jsonPath(".contact[]")
            .valueReceived(purpose)
            .supportedValues(Arrays.stream(ContactPurpose.values()).toList())
            .build();
    }
  }

  private Set<WriteableFilemanValue> contactAppeals(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    contactCompanyName(contact, InsuranceCompany.APPEALS_COMPANY_NAME)
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.APPEALS_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw MissingRequiredField.builder().jsonPath(".contact[]").build();
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(filemanFactory.toString(InsuranceCompany.APPEALS_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            ".contact[].address",
            ContactPurpose.APPEAL.name(),
            InsuranceCompany.APPEALS_ADDRESS_ST_LINE_1_,
            InsuranceCompany.APPEALS_ADDRESS_ST_LINE_2_,
            InsuranceCompany.APPEALS_ADDRESS_ST_LINE_3_,
            InsuranceCompany.APPEALS_ADDRESS_CITY,
            InsuranceCompany.APPEALS_ADDRESS_STATE,
            InsuranceCompany.APPEALS_ADDRESS_ZIP,
            contact.address()));
    return contactDetails;
  }

  private Set<WriteableFilemanValue> contactBilling(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    contactCompanyName(contact, InsuranceCompany.BILLING_COMPANY_NAME)
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw MissingRequiredListItem.builder()
                  .jsonPath(".contact[].extension[]")
                  .qualifiers(
                      List.of(
                          ".contact[].purpose = " + ContactPurpose.BILL,
                          ".contact.extension[].url = "
                              + OrganizationStructureDefinitions.VIA_INTERMEDIARY))
                  .build();
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.BILLING_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw MissingRequiredListItem.builder()
                  .jsonPath(".contact[].telecom[].value")
                  .qualifiers(
                      List.of(
                          ".contact[].purpose = " + ContactPurpose.BILL,
                          ".contact[].telecom[].system =" + ContactPointSystem.phone))
                  .build();
            });
    return contactDetails;
  }

  Optional<WriteableFilemanValue> contactCompanyName(Contact contact, String fieldNumber) {
    Optional<Extension> maybeCompanyNameExtension =
        extensionForSystem(contact.extension(), OrganizationStructureDefinitions.VIA_INTERMEDIARY);
    if (maybeCompanyNameExtension.isEmpty()) {
      return Optional.empty();
    }
    Reference companyNameReference = maybeCompanyNameExtension.get().valueReference();
    if (isBlank(companyNameReference)) {
      return Optional.empty();
    }
    return filemanFactory.forString(fieldNumber, 1, companyNameReference.display());
  }

  private Set<WriteableFilemanValue> contactDentalClaims(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    contactCompanyName(contact, InsuranceCompany.CLAIMS_DENTAL_COMPANY_NAME)
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_DENTAL_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_DENTAL_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            ".contact[].address",
            ContactPurpose.DENTALCLAIMS.name(),
            InsuranceCompany.CLAIMS_DENTAL_STREET_ADDR_1,
            InsuranceCompany.CLAIMS_DENTAL_STREET_ADDR_2,
            null,
            InsuranceCompany.CLAIMS_DENTAL_PROCESS_CITY,
            InsuranceCompany.CLAIMS_DENTAL_PROCESS_STATE,
            InsuranceCompany.CLAIMS_DENTAL_PROCESS_ZIP,
            contact.address()));
    return contactDetails;
  }

  private Set<WriteableFilemanValue> contactInpatientClaims(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    contactCompanyName(contact, InsuranceCompany.CLAIMS_INPT_COMPANY_NAME)
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw MissingRequiredListItem.builder()
                  .jsonPath(".contact[].telecom[].value")
                  .qualifiers(
                      List.of(
                          ".contact[].purpose = " + ContactPurpose.INPTCLAIMS,
                          ".contact[].telecom[].system =" + ContactPointSystem.phone))
                  .build();
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_INPT_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            ".contact[].address",
            ContactPurpose.INPTCLAIMS.name(),
            InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_1,
            InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_2,
            InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_3,
            InsuranceCompany.CLAIMS_INPT_PROCESS_CITY,
            InsuranceCompany.CLAIMS_INPT_PROCESS_STATE,
            InsuranceCompany.CLAIMS_INPT_PROCESS_ZIP,
            contact.address()));
    return contactDetails;
  }

  private Set<WriteableFilemanValue> contactInquiry(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    contactCompanyName(contact, InsuranceCompany.INQUIRY_COMPANY_NAME)
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.INQUIRY_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw MissingRequiredListItem.builder()
                  .jsonPath(".contact[].telecom[].value")
                  .qualifiers(
                      List.of(
                          ".contact[].purpose = " + ContactPurpose.INQUIRY,
                          ".contact[].telecom[].system =" + ContactPointSystem.phone))
                  .build();
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(filemanFactory.toString(InsuranceCompany.INQUIRY_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            ".contact[].address",
            ContactPurpose.INQUIRY.name(),
            InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_1_,
            InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_2_,
            InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_3_,
            InsuranceCompany.INQUIRY_ADDRESS_CITY,
            InsuranceCompany.INQUIRY_ADDRESS_STATE,
            InsuranceCompany.INQUIRY_ADDRESS_ZIP_CODE,
            contact.address()));
    return contactDetails;
  }

  private Set<WriteableFilemanValue> contactOutpatientClaims(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    contactCompanyName(contact, InsuranceCompany.CLAIMS_OPT_COMPANY_NAME)
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw MissingRequiredListItem.builder()
                  .jsonPath(".contact[].telecom[].value")
                  .qualifiers(
                      List.of(
                          ".contact[].purpose = " + ContactPurpose.OUTPTCLAIMS,
                          ".contact[].telecom[].system =" + ContactPointSystem.phone))
                  .build();
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(
            filemanFactory.toString(InsuranceCompany.CLAIMS_OPT_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            ".contact[].address",
            ContactPurpose.OUTPTCLAIMS.name(),
            InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_1,
            InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_2,
            InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_3,
            InsuranceCompany.CLAIMS_OPT_PROCESS_CITY,
            InsuranceCompany.CLAIMS_OPT_PROCESS_STATE,
            InsuranceCompany.CLAIMS_OPT_PROCESS_ZIP,
            contact.address()));
    return contactDetails;
  }

  WriteableFilemanValue contactPoint(
      List<ContactPoint> telecom,
      String fieldNumber,
      String contactType,
      ContactPointSystem system) {
    if (isBlank(telecom)) {
      throw MissingRequiredField.builder().jsonPath(".telecom[]").build();
    }

    return contactPointForSystem(telecom, system)
        .flatMap(cp -> insuranceCompanyCoordinates(fieldNumber, 1, cp.value()))
        .orElseThrow(
            () ->
                MissingRequiredListItem.builder()
                    .jsonPath(".telecom[].value")
                    .qualifiers(List.of(".contact[].telecom[].system = " + system))
                    .build());
  }

  Optional<ContactPoint> contactPointForSystem(
      List<ContactPoint> contactPoints, ContactPointSystem system) {
    if (isBlank(contactPoints)) {
      return Optional.empty();
    }
    return contactPoints.stream().filter(c -> system.equals(c.system())).findFirst();
  }

  private Set<WriteableFilemanValue> contactPrecert(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    contactCompanyName(contact, InsuranceCompany.PRECERT_COMPANY_NAME)
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw MissingRequiredListItem.builder()
                  .jsonPath(".contact[].telecom[].value")
                  .qualifiers(
                      List.of(
                          ".contact[].purpose = " + ContactPurpose.PRECERT,
                          ".contact[].telecom[].system =" + ContactPointSystem.phone))
                  .build();
            });
    return contactDetails;
  }

  ContactPurpose contactPurposeOrDie(CodeableConcept purpose) {
    /* TODO https://vajira.max.gov/browse/API-10394 ignore unknown contacts. */
    if (isBlank(purpose)) {
      throw MissingRequiredField.builder().jsonPath(".contact[].purpose").build();
    }
    if (isBlank(purpose.coding())) {
      throw MissingRequiredField.builder().jsonPath(".contact[].purpose.coding[]").build();
    }
    if (purpose.coding().size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .receivedCount(purpose.coding().size())
          .exactExpectedCount(1)
          .jsonPath(".contact[].purpose")
          .build();
    }
    /* TODO https://vajira.max.gov/browse/API-11250 Fix system */
    var purposeCode = purpose.coding().get(0).code();
    if (isBlank(purposeCode)) {
      throw MissingRequiredField.builder().jsonPath(".contact[].purpose.coding[].code").build();
    }
    return ContactPurpose.valueOf(purposeCode);
  }

  private Set<WriteableFilemanValue> contactRxClaims(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    contactCompanyName(contact, InsuranceCompany.CLAIMS_RX_COMPANY_NAME)
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_RX_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(filemanFactory.toString(InsuranceCompany.CLAIMS_RX_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            ".contact[].address",
            ContactPurpose.RXCLAIMS.name(),
            InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_1,
            InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_2,
            InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_3,
            InsuranceCompany.CLAIMS_RX_CITY,
            InsuranceCompany.CLAIMS_RX_STATE,
            InsuranceCompany.CLAIMS_RX_ZIP,
            contact.address()));
    return contactDetails;
  }

  private Set<WriteableFilemanValue> contactVerify(Contact contact) {
    var phone =
        contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
            .map(
                filemanFactory.toString(
                    InsuranceCompany.VERIFICATION_PHONE_NUMBER, index(1), ContactPoint::value))
            .orElseThrow(
                () ->
                    MissingRequiredListItem.builder()
                        .jsonPath(".contact[].telecom[].value")
                        .qualifiers(
                            List.of(
                                ".contact[].purpose = " + ContactPurpose.VERIFY,
                                ".contact[].telecom[].system =" + ContactPointSystem.phone))
                        .build());
    return Set.of(phone);
  }

  private Set<WriteableFilemanValue> contacts() {
    Set<ContactPurpose> requiredContacts =
        new HashSet<>(
            Set.of(
                ContactPurpose.APPEAL,
                ContactPurpose.BILL,
                ContactPurpose.INPTCLAIMS,
                ContactPurpose.OUTPTCLAIMS,
                ContactPurpose.INQUIRY,
                ContactPurpose.PRECERT,
                ContactPurpose.VERIFY));
    var contacts =
        Safe.stream(organization().contact())
            .map(
                contact -> {
                  var purpose = contactPurposeOrDie(contact.purpose());
                  requiredContacts.remove(purpose);
                  return contact(purpose, contact);
                })
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    if (requiredContacts.isEmpty()) {
      return contacts;
    }
    throw MissingRequiredListItem.builder()
        .jsonPath(".contact[]")
        .qualifiers(List.of(".contact[].purpose = " + requiredContacts))
        .build();
  }

  Optional<Extension> extensionForSystem(List<Extension> extensions, String system) {
    if (isBlank(extensions)) {
      return Optional.empty();
    }
    return extensions.stream().filter(e -> system.equals(e.url())).findFirst();
  }

  private List<ExtensionHandler> extensionHandlers() {
    return Stream.of(
            extensionHandlersBoolean(),
            extensionHandlersCodeableConcept(),
            extensionHandlersReference(),
            extensionHandlersQuantity(),
            extensionHandlersString())
        .flatMap(Collection::stream)
        .toList();
  }

  private Set<ExtensionHandler> extensionHandlersBoolean() {
    return Set.of(
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.SIGNATURE_REQUIRED_ON_BILL)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.SIGNATURE_REQUIRED_ON_BILL_)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions
                    .ATTENDING_RENDERING_PROVIDER_SECONDARY_IDPROFESIONAL_REQUIRED)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_PROF)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions
                    .ATTENDING_RENDERING_PROVIDER_SECONDARY_IDINSTITUTIONAL_REQUIRED)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_INST)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PRINT_SEC_TERT_AUTO_CLAIMS_LOCALLY)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PRINT_SEC_TERT_AUTO_CLAIMS_)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PRINT_SEC_MED_CLAIMS_WOMRALOCALLY)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PRINT_SEC_MED_CLAIMS_W_O_MRA_)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ALLOW_MULTIPLE_BEDSECTIONS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ALLOW_MULTIPLE_BEDSECTIONS)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ONE_OUTPAT_VISIT_ON_BILL_ONLY)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ONE_OPT_VISIT_ON_BILL_ONLY)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_INPAT_CLAIMS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_IP_CLAIMS_)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_APPEALS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_APPEALS_)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_INQUIRIES)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_INQUIRIES_)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_DENTAL_CLAIMS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROC_DENT_CLAIMS_)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_OUTPAT_CLAIMS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_OP_CLAIMS_)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_PRECERT)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_PRECERTS_)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_RX_CLAIMS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_RX_CLAIMS_)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build());
  }

  private Set<ExtensionHandler> extensionHandlersCodeableConcept() {
    return Set.of(
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.TYPE_OF_COVERAGE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.TYPE_OF_COVERAGE)
            .index(1)
            .codingSystem(OrganizationStructureDefinitions.TYPE_OF_COVERAGE_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.WILL_REIMBURSE_FOR_CARE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.REIMBURSE_)
            .index(1)
            .codingSystem(OrganizationStructureDefinitions.WILL_REIMBURSE_FOR_CARE_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ELECTRONIC_TRANSMISSION_MODE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.TRANSMIT_ELECTRONICALLY)
            .index(1)
            .codingSystem(OrganizationStructureDefinitions.ELECTRONIC_TRANSMISSION_MODE_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ELECTRONIC_INSURANCE_TYPE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ELECTRONIC_INSURANCE_TYPE)
            .index(1)
            .codingSystem(OrganizationStructureDefinitions.ELECTRONIC_INSURANCE_TYPE_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.REFERRING_PROVIDER_SECOND_IDTYPE_UB_04)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.REF_PROV_SEC_ID_REQ_ON_CLAIMS)
            .index(1)
            .codingSystem(
                OrganizationStructureDefinitions.REFERRING_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.AMBULATORY_SURGERY_REVENUE_CODE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.AMBULATORY_SURG_REV_CODE)
            .index(1)
            .codingSystem(OrganizationStructureDefinitions.AMBULATORY_SURGERY_REVENUE_CODE_URN_OID)
            .required(OPTIONAL)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PRESCRIPTION_REVENUE_CODE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PRESCRIPTION_REFILL_REV_CODE)
            .index(1)
            .codingSystem(OrganizationStructureDefinitions.PRESCRIPTION_REVENUE_CODE_URN_OID)
            .required(OPTIONAL)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_1500)
            .index(1)
            .codingSystem(
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID)
            .required(OPTIONAL)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_UB)
            .index(1)
            .codingSystem(
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID)
            .required(OPTIONAL)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.REFERRING_PROVIDER_SECOND_IDTYPE_CMS_1500)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.REF_PROV_SEC_ID_DEF_CMS_1500)
            .index(1)
            .codingSystem(
                OrganizationStructureDefinitions.REFERRING_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID)
            .required(OPTIONAL)
            .build());
  }

  private Set<ExtensionHandler> extensionHandlersQuantity() {
    return Set.of(
        QuantityExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PLAN_STANDARD_FILING_TIME_FRAME)
            .required(REQUIRED)
            .valueFieldNumber(InsuranceCompany.STANDARD_FTF_VALUE)
            .unitFieldNumber(InsuranceCompany.STANDARD_FTF)
            .index(1)
            .filemanFactory(filemanFactory)
            .build());
  }

  private Set<ExtensionHandler> extensionHandlersReference() {
    return Set.of(
        ReferenceExtensionHandler.forDefiningUrl(OrganizationStructureDefinitions.VIA_INTERMEDIARY)
            .required(REQUIRED)
            .fieldNumber(InsuranceCompany.PAYER)
            .index(1)
            .referenceType("Organization")
            .toCoordinates(RecordCoordinates::fromString)
            .filemanFactory(filemanFactory)
            .build());
  }

  private Set<ExtensionHandler> extensionHandlersString() {
    return Set.of(
        StringExtensionHandler.forDefiningUrl(OrganizationStructureDefinitions.FILING_TIME_FRAME)
            .fieldNumber(InsuranceCompany.FILING_TIME_FRAME)
            .index(1)
            .filemanFactory(filemanFactory)
            .required(REQUIRED)
            .build());
  }

  Optional<WriteableFilemanValue> identifier(
      String fieldNumber, String identifierCode, boolean required) {
    Optional<Identifier> identifier =
        identifierForPredicate(organization().identifier(), c -> identifierCode.equals(c.code()));
    if (identifier.isEmpty() && required) {
      throw MissingRequiredListItem.builder()
          .jsonPath(".identifier[]")
          .qualifiers(List.of(".identifier[].type.coding[].code is" + identifierCode))
          .build();
    }
    if (identifier.isEmpty()) {
      return Optional.empty();
    }
    if (identifier.get().value().isBlank()) {
      throw MissingRequiredListItem.builder()
          .jsonPath(".identifier[]")
          .qualifiers(List.of(".identifier[].type.coding[].code is" + identifierCode))
          .build();
    }
    return filemanFactory.forString(fieldNumber, 1, identifier.get().value());
  }

  Optional<Identifier> identifierForPredicate(
      List<Identifier> identifiers, Predicate<Coding> predicate) {
    if (isBlank(identifiers)) {
      return Optional.empty();
    }
    return identifiers.stream()
        .filter(
            i -> {
              if (i.type() == null) {
                return false;
              }
              if (i.type().coding() == null) {
                return false;
              }
              return i.type().coding().stream().anyMatch(predicate);
            })
        .findFirst();
  }

  @SuppressWarnings("SameParameterValue")
  private Optional<WriteableFilemanValue> insuranceCompanyCoordinates(
      String field, int index, String value) {
    return filemanFactory.forString(field, index, value);
  }

  List<WriteableFilemanValue> n277EdiIdentifier() {
    Optional<Identifier> identifier =
        identifierForPredicate(
            organization().identifier(),
            c -> OrganizationStructureDefinitions.N277_EDI_ID_NUMBER_CODE.equals(c.code()));
    if (identifier.isEmpty()) {
      throw MissingRequiredListItem.builder()
          .jsonPath(".identifier[]")
          .qualifiers(
              List.of(
                  ".identifier[].type.coding[].code = "
                      + OrganizationStructureDefinitions.N277_EDI_ID_NUMBER_CODE))
          .build();
    }
    if (identifier.get().value().isBlank()) {
      throw MissingRequiredListItem.builder()
          .jsonPath(".identifier[].value")
          .qualifiers(
              List.of(
                  ".identifier[].type.coding[].code = "
                      + OrganizationStructureDefinitions.N277_EDI_ID_NUMBER_CODE))
          .build();
    }
    ArrayList<WriteableFilemanValue> n277Values = new ArrayList<>(2);
    n277Values.add(
        WriteableFilemanValue.builder()
            .field("IEN")
            .index(1)
            .file(N277EdiIdNumber.FILE_NUMBER)
            .value("${36^1^IEN}")
            .build());
    n277Values.add(
        WriteableFilemanValue.builder()
            .field(N277EdiIdNumber.N277EDI_ID_NUMBER)
            .index(1)
            .file(N277EdiIdNumber.FILE_NUMBER)
            .value(identifier.get().value())
            .build());
    return n277Values;
  }

  List<WriteableFilemanValue> optionalIdentifierFieldAndQualifier(
      String idField, String qualifierField, String identifierSystem) {
    Optional<Identifier> identifier =
        identifierForPredicate(
            organization().identifier(), c -> identifierSystem.equals(c.system()));
    if (isBlank(identifier)) {
      return List.of();
    }
    if (isBlank(identifier.get().value())) {
      throw MissingRequiredListItem.builder()
          .jsonPath(".identifier[].value")
          .qualifiers(List.of(".identifier[].type.coding[].system = " + identifierSystem))
          .build();
    }
    var matchingCodes =
        identifier.get().type().coding().stream().map(Coding::code).collect(Collectors.toList());
    if (matchingCodes.isEmpty()) {
      throw MissingRequiredListItem.builder()
          .jsonPath(".identifier[].type.coding[].code")
          .qualifiers(List.of(".identifier[].type.coding[].system = " + identifierSystem))
          .build();
    }
    if (matchingCodes.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".identifier[].type.coding[].code")
          .receivedCount(matchingCodes.size())
          .exactExpectedCount(1)
          .build();
    }
    return List.of(
        filemanFactory.forString(idField, 1, identifier.get().value()).get(),
        filemanFactory.forString(qualifierField, 1, matchingCodes.get(0)).get());
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceCompanyFile() {
    /* TODO https://vajira.max.gov/browse/API-10394 validate active status. */
    /* TODO https://vajira.max.gov/browse/API-10394 insurance company type. */
    Set<WriteableFilemanValue> fields = new HashSet<>();
    addRequiredFields(fields);
    Optional.ofNullable(organization().id())
        .map(RecordCoordinates::fromString)
        .map(filemanFactory.recordCoordinatesToPointer(InsuranceCompany.FILE_NUMBER, index(1)))
        .ifPresent(fields::add);
    return fields;
  }

  public enum ContactPurpose {
    APPEAL,
    BILL,
    DENTALCLAIMS,
    INPTCLAIMS,
    OUTPTCLAIMS,
    INQUIRY,
    PRECERT,
    RXCLAIMS,
    VERIFY
  }
}
