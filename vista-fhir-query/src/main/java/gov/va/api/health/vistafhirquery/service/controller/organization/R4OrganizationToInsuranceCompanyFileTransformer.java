package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.OPTIONAL;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

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
import gov.va.api.health.vistafhirquery.service.controller.ExtensionProcessor;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.BooleanExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.CodeableConceptExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.QuantityExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ReferenceExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.StringExtensionHandler;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
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

@Value
public class R4OrganizationToInsuranceCompanyFileTransformer {
  static final Map<Boolean, String> YES_NO = Map.of(true, "YES", false, "NO");

  private static final WriteableFilemanValueFactory filemanFactory =
      WriteableFilemanValueFactory.forFile(InsuranceCompany.FILE_NUMBER);

  @NonNull Organization organization;

  ExtensionProcessor extensionProcessor = R4ExtensionProcessor.of(extensionHandlers());

  @Builder
  R4OrganizationToInsuranceCompanyFileTransformer(Organization organization) {
    this.organization = organization;
  }

  private void addRequiredFields(Set<WriteableFilemanValue> fields) {
    fields.add(
        insuranceCompanyCoordinatesOrDie(InsuranceCompany.NAME, 1, organization.name(), "name"));
    fields.addAll(
        address(
            InsuranceCompany.STREET_ADDRESS_LINE_1_,
            InsuranceCompany.STREET_ADDRESS_LINE_2_,
            InsuranceCompany.STREET_ADDRESS_LINE_3_,
            InsuranceCompany.CITY,
            InsuranceCompany.STATE,
            InsuranceCompany.ZIP_CODE,
            addressOrDie(organization.address())));
    fields.addAll(contacts());
    fields.add(
        contactPoint(
            organization.telecom(),
            InsuranceCompany.FAX_NUMBER,
            "organization",
            ContactPointSystem.fax));
    fields.add(
        contactPoint(
            organization.telecom(),
            InsuranceCompany.PHONE_NUMBER,
            "organization",
            ContactPointSystem.phone));
    fields.addAll(extensionProcessor.process(organization.extension()));
    fields.add(
        identifier(
                "edi id number inst",
                InsuranceCompany.EDI_ID_NUMBER_INST,
                OrganizationStructureDefinitions.EDI_ID_NUMBER_INST_CODE,
                true)
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        InsuranceCompany.EDI_ID_NUMBER_INST,
                        "edi id number inst is a required identifier.")));
    fields.add(
        identifier(
                "edi id number prof",
                InsuranceCompany.EDI_ID_NUMBER_PROF,
                OrganizationStructureDefinitions.EDI_ID_NUMBER_PROF_CODE,
                true)
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        InsuranceCompany.EDI_ID_NUMBER_PROF,
                        "edi id number prof is a required identifier.")));
    identifier(
            "bin number",
            InsuranceCompany.BIN_NUMBER,
            OrganizationStructureDefinitions.BIN_NUMBER_CODE,
            false)
        .ifPresent(fields::add);
    identifier(
            "edi id number - dental",
            InsuranceCompany.EDI_ID_NUMBER_DENTAL,
            OrganizationStructureDefinitions.EDI_ID_NUMBER_DENTAL_CODE,
            false)
        .ifPresent(fields::add);
    fields.addAll(
        identifierSlice(
            "edi inst secondary id (1)",
            InsuranceCompany.EDI_INST_SECONDARY_ID_1_,
            InsuranceCompany.EDI_INST_SECONDARY_ID_QUAL_1_,
            OrganizationStructureDefinitions.EDI_INST_SECONDARY_ID_QUAL_1,
            false));
    fields.addAll(
        identifierSlice(
            "edi id inst secondary id (2)",
            InsuranceCompany.EDI_INST_SECONDARY_ID_2_,
            InsuranceCompany.EDI_INST_SECONDARY_ID_QUAL_2_,
            OrganizationStructureDefinitions.EDI_INST_SECONDARY_ID_QUAL_2,
            false));
    fields.addAll(
        identifierSlice(
            "edi prof secondary id (1)",
            InsuranceCompany.EDI_PROF_SECONDARY_ID_1_,
            InsuranceCompany.EDI_PROF_SECONDARY_ID_QUAL_1_,
            OrganizationStructureDefinitions.EDI_PROF_SECONDARY_ID_QUAL_1,
            false));
    fields.addAll(
        identifierSlice(
            "edi prof inst secondary id (2)",
            InsuranceCompany.EDI_PROF_SECONDARY_ID_2_,
            InsuranceCompany.EDI_PROF_SECONDARY_ID_QUAL_2_,
            OrganizationStructureDefinitions.EDI_PROF_SECONDARY_ID_QUAL_2,
            false));
  }

  private Set<WriteableFilemanValue> address(
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
    String[] arrayLines = new String[] {streetLine1, streetLine2, streetLine3};
    if (isBlank(lines)) {
      lines = emptyList();
    }
    for (int i = 0; i < lines.size(); i++) {
      addressValues.add(filemanFactory.forString(arrayLines[i], 1, lines.get(i)));
    }
    addressValues.add(filemanFactory.forString(city, 1, address.city()));
    addressValues.add(filemanFactory.forString(state, 1, address.state()));
    addressValues.add(filemanFactory.forString(zipcode, 1, address.postalCode()));
    return addressValues;
  }

  private Address addressOrDie(List<Address> address) {
    if (address.size() != 1) {
      throw BadRequestPayload.because("Organization.address is required.");
    }
    return address.get(0);
  }

  private Set<WriteableFilemanValue> appealsContact(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    companyName(contact, InsuranceCompany.APPEALS_COMPANY_NAME).ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.APPEALS_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceCompany.APPEALS_PHONE_NUMBER,
                  "Unable to populate phone for APPEAL contact.");
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(filemanFactory.toString(InsuranceCompany.APPEALS_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            InsuranceCompany.APPEALS_ADDRESS_ST_LINE_1_,
            InsuranceCompany.APPEALS_ADDRESS_ST_LINE_2_,
            InsuranceCompany.APPEALS_ADDRESS_ST_LINE_3_,
            InsuranceCompany.APPEALS_ADDRESS_CITY,
            InsuranceCompany.APPEALS_ADDRESS_STATE,
            InsuranceCompany.APPEALS_ADDRESS_ZIP,
            contact.address()));
    return contactDetails;
  }

  private Set<WriteableFilemanValue> billingContact(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    companyName(contact, InsuranceCompany.BILLING_COMPANY_NAME)
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceCompany.BILLING_COMPANY_NAME,
                  "Cannot populate name field for BILL contact.");
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.BILLING_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceCompany.BILLING_PHONE_NUMBER, "Cannot populate phone for BILL contact.");
            });
    return contactDetails;
  }

  private Set<ExtensionHandler> booleanHandlers() {
    return Set.of(
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.SIGNATURE_REQUIRED_ON_BILL)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.SIGNATURE_REQUIRED_ON_BILL_)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions
                    .ATTENDING_RENDERING_PROVIDER_SECONDARY_IDPROFESIONAL_REQUIRED)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_PROF)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions
                    .ATTENDING_RENDERING_PROVIDER_SECONDARY_IDINSTITUTIONAL_REQUIRED)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_INST)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PRINT_SEC_TERT_AUTO_CLAIMS_LOCALLY)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PRINT_SEC_TERT_AUTO_CLAIMS_)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PRINT_SEC_MED_CLAIMS_WOMRALOCALLY)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PRINT_SEC_MED_CLAIMS_W_O_MRA_)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ALLOW_MULTIPLE_BEDSECTIONS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ALLOW_MULTIPLE_BEDSECTIONS)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ONE_OUTPAT_VISIT_ON_BILL_ONLY)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ONE_OPT_VISIT_ON_BILL_ONLY)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_INPAT_CLAIMS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_IP_CLAIMS_)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_APPEALS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_APPEALS_)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_INQUIRIES)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_INQUIRIES_)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_DENTAL_CLAIMS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROC_DENT_CLAIMS_)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_OUTPAT_CLAIMS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_OP_CLAIMS_)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_PRECERT)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_PRECERTS_)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_RX_CLAIMS)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ANOTHER_CO_PROCESS_RX_CLAIMS_)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build());
  }

  private Set<WriteableFilemanValue> claimsInptContact(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    companyName(contact, InsuranceCompany.CLAIMS_INPT_COMPANY_NAME).ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER,
                  "Unable to populate phone for INPTCLAIMS contact.");
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_INPT_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_1,
            InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_2,
            InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_3,
            InsuranceCompany.CLAIMS_INPT_PROCESS_CITY,
            InsuranceCompany.CLAIMS_INPT_PROCESS_STATE,
            InsuranceCompany.CLAIMS_INPT_PROCESS_ZIP,
            contact.address()));
    return contactDetails;
  }

  private Set<WriteableFilemanValue> claimsOptContact(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    companyName(contact, InsuranceCompany.CLAIMS_OPT_COMPANY_NAME).ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER,
                  "Unable to populate phone for OUTPTCLAIMS contact.");
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(
            filemanFactory.toString(InsuranceCompany.CLAIMS_OPT_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_1,
            InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_2,
            InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_3,
            InsuranceCompany.CLAIMS_OPT_PROCESS_CITY,
            InsuranceCompany.CLAIMS_OPT_PROCESS_STATE,
            InsuranceCompany.CLAIMS_OPT_PROCESS_ZIP,
            contact.address()));
    return contactDetails;
  }

  private Set<ExtensionHandler> codeableConceptHandlers() {
    return Set.of(
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.TYPE_OF_COVERAGE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.TYPE_OF_COVERAGE)
            .codingSystem(OrganizationStructureDefinitions.TYPE_OF_COVERAGE_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.WILL_REIMBURSE_FOR_CARE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.REIMBURSE_)
            .codingSystem(OrganizationStructureDefinitions.WILL_REIMBURSE_FOR_CARE_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ELECTRONIC_TRANSMISSION_MODE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.TRANSMIT_ELECTRONICALLY)
            .codingSystem(OrganizationStructureDefinitions.ELECTRONIC_TRANSMISSION_MODE_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.ELECTRONIC_INSURANCE_TYPE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.ELECTRONIC_INSURANCE_TYPE)
            .codingSystem(OrganizationStructureDefinitions.ELECTRONIC_INSURANCE_TYPE_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.REFERRNG_PROVIDER_SECOND_IDTYPE_UB_04)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.REF_PROV_SEC_ID_REQ_ON_CLAIMS)
            .codingSystem(
                OrganizationStructureDefinitions.REFERRNG_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.AMBULATORY_SURGERY_REVENUE_CODE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.AMBULATORY_SURG_REV_CODE)
            .codingSystem(OrganizationStructureDefinitions.AMBULATORY_SURGERY_REVENUE_CODE_URN_OID)
            .required(OPTIONAL)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PRESCRIPTION_REVENUE_CODE)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PRESCRIPTION_REFILL_REV_CODE)
            .codingSystem(OrganizationStructureDefinitions.PRESCRIPTION_REVENUE_CODE_URN_OID)
            .required(OPTIONAL)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_1500)
            .codingSystem(
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID)
            .required(OPTIONAL)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_UB)
            .codingSystem(
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID)
            .required(OPTIONAL)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.REFERRNG_PROVIDER_SECOND_IDTYPE_CMS_1500)
            .filemanFactory(filemanFactory)
            .fieldNumber(InsuranceCompany.REF_PROV_SEC_ID_DEF_CMS_1500)
            .codingSystem(
                OrganizationStructureDefinitions.REFERRNG_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID)
            .required(OPTIONAL)
            .build());
  }

  Optional<Coding> codingForSystem(List<Coding> codings, String system) {
    if (isBlank(codings)) {
      return Optional.empty();
    }
    return codings.stream().filter(c -> system.equals(c.system())).findFirst();
  }

  Optional<WriteableFilemanValue> companyName(Contact contact, String fieldNumber) {
    Optional<Extension> maybeCompanyNameExtension =
        extensionForSystem(contact.extension(), OrganizationStructureDefinitions.VIA_INTERMEDIARY);
    if (maybeCompanyNameExtension.isEmpty()) {
      return Optional.empty();
    }
    Reference billingCompanyNameReference = maybeCompanyNameExtension.get().valueReference();
    if (isBlank(billingCompanyNameReference)) {
      return Optional.empty();
    }
    return Optional.ofNullable(
        filemanFactory.forString(fieldNumber, 1, billingCompanyNameReference.display()));
  }

  private Set<WriteableFilemanValue> contact(
      @NonNull ContactPurpose purpose, @NonNull Contact contact) {
    switch (purpose) {
      case APPEAL:
        return appealsContact(contact);
      case BILL:
        return billingContact(contact);
      case DENTALCLAIMS:
        return dentalClaimsContact(contact);
      case INPTCLAIMS:
        return claimsInptContact(contact);
      case OUTPTCLAIMS:
        return claimsOptContact(contact);
      case INQUIRY:
        return inquiryContact(contact);
      case PRECERT:
        return precertContact(contact);
      case RXCLAIMS:
        return rxClaimsContact(contact);
      case VERIFY:
        return verificationContact(contact);
      default:
        throw BadRequestPayload.because("Unknown purpose for contact: " + contact);
    }
  }

  WriteableFilemanValue contactPoint(
      List<ContactPoint> telecom,
      String fieldNumber,
      String contactType,
      ContactPointSystem system) {
    if (isBlank(telecom)) {
      throw BadRequestPayload.because(fieldNumber, contactType + "telecom reference is null");
    }
    ContactPoint contactPoint =
        contactPointForSystem(telecom, system)
            .orElseThrow(
                () ->
                    BadRequestPayload.because(fieldNumber, contactType + " contact point is null"));
    return insuranceCompanyCoordinatesOrDie(
        fieldNumber, 1, contactPoint.value(), contactType + " contact point value");
  }

  Optional<ContactPoint> contactPointForSystem(
      List<ContactPoint> contactPoints, ContactPointSystem system) {
    if (isBlank(contactPoints)) {
      return Optional.empty();
    }
    return contactPoints.stream().filter(c -> system.equals(c.system())).findFirst();
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
        organization().contact().stream()
            .map(
                contact -> {
                  var purpose = purposeOrDie(contact.purpose());
                  requiredContacts.remove(purpose);
                  return contact(purpose, contact);
                })
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    if (requiredContacts.isEmpty()) {
      return contacts;
    }
    throw BadRequestPayload.because("Payload missing required contacts types: " + requiredContacts);
  }

  private Set<WriteableFilemanValue> dentalClaimsContact(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    companyName(contact, InsuranceCompany.CLAIMS_DENTAL_COMPANY_NAME)
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
            InsuranceCompany.CLAIMS_DENTAL_STREET_ADDR_1,
            InsuranceCompany.CLAIMS_DENTAL_STREET_ADDR_2,
            null,
            InsuranceCompany.CLAIMS_DENTAL_PROCESS_CITY,
            InsuranceCompany.CLAIMS_DENTAL_PROCESS_STATE,
            InsuranceCompany.CLAIMS_DENTAL_PROCESS_ZIP,
            contact.address()));
    return contactDetails;
  }

  Optional<Extension> extensionForSystem(List<Extension> extensions, String system) {
    if (isBlank(extensions)) {
      return Optional.empty();
    }
    return extensions.stream().filter(e -> system.equals(e.url())).findFirst();
  }

  private List<ExtensionHandler> extensionHandlers() {
    return Stream.of(
            booleanHandlers(),
            codeableConceptHandlers(),
            referenceHandlers(),
            quantityHandlers(),
            stringHandlers())
        .flatMap(Collection::stream)
        .toList();
  }

  Optional<WriteableFilemanValue> identifier(
      String fieldName, String fieldNumber, String identifierCode, boolean required) {
    Optional<Identifier> identifier =
        identifierForPredicate(organization.identifier(), c -> identifierCode.equals(c.code()));
    if (identifier.isEmpty() && required) {
      throw BadRequestPayload.because(fieldNumber, fieldName + " identifier is required");
    }
    if (identifier.isEmpty()) {
      return Optional.empty();
    }
    if (identifier.get().value().isBlank()) {
      throw BadRequestPayload.because(fieldNumber, fieldName + "identifier.value is null");
    }
    return Optional.ofNullable(filemanFactory.forString(fieldNumber, 1, identifier.get().value()));
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

  List<WriteableFilemanValue> identifierSlice(
      String fieldName,
      String idField,
      String qualifierField,
      String identifierSystem,
      boolean required) {
    Optional<Identifier> identifier =
        identifierForPredicate(organization.identifier(), c -> identifierSystem.equals(c.system()));
    if (identifier.isEmpty() && required) {
      throw BadRequestPayload.because(idField, fieldName + " identifier slice is required");
    }
    if (identifier.isEmpty()) {
      return List.of();
    }
    if (identifier.get().value().isBlank()) {
      throw BadRequestPayload.because(idField, fieldName + " identifier.value is null");
    }
    var matchingCodes =
        identifier.get().type().coding().stream().map(Coding::code).collect(Collectors.toList());
    if (matchingCodes.isEmpty()) {
      throw BadRequestPayload.because(idField, fieldName + " identifier.type.coding.code is null");
    }
    if (matchingCodes.size() > 1) {
      throw BadRequestPayload.because(
          idField, fieldName + " identifier.type.coding.code only one code is allowed.");
    }
    return List.of(
        filemanFactory.forString(idField, 1, identifier.get().value()),
        filemanFactory.forString(qualifierField, 1, matchingCodes.get(0)));
  }

  private Set<WriteableFilemanValue> inquiryContact(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    companyName(contact, InsuranceCompany.INQUIRY_COMPANY_NAME).ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.INQUIRY_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceCompany.INQUIRY_PHONE_NUMBER,
                  "Could not populate phone for INQUIRY contact.");
            });
    contactPointForSystem(contact.telecom(), ContactPointSystem.fax)
        .map(filemanFactory.toString(InsuranceCompany.INQUIRY_FAX, index(1), ContactPoint::value))
        .ifPresent(contactDetails::add);
    contactDetails.addAll(
        address(
            InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_1_,
            InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_2_,
            InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_3_,
            InsuranceCompany.INQUIRY_ADDRESS_CITY,
            InsuranceCompany.INQUIRY_ADDRESS_STATE,
            InsuranceCompany.INQUIRY_ADDRESS_ZIP_CODE,
            contact.address()));
    return contactDetails;
  }

  private WriteableFilemanValue insuranceCompanyCoordinatesOrDie(
      String field, Integer index, String value, String fieldName) {
    var wfv = filemanFactory.forString(field, index, value);
    if (isBlank(value)) {
      throw BadRequestPayload.because(field, fieldName + " is null");
    }
    return wfv;
  }

  private Set<WriteableFilemanValue> precertContact(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    companyName(contact, InsuranceCompany.PRECERT_COMPANY_NAME).ifPresent(contactDetails::add);
    contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
        .map(
            filemanFactory.toString(
                InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER, index(1), ContactPoint::value))
        .ifPresentOrElse(
            contactDetails::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER,
                  "Unable to populate phone for PRECERT contact.");
            });
    return contactDetails;
  }

  private ContactPurpose purposeOrDie(CodeableConcept purpose) {
    if (isBlank(purpose)) {
      throw BadRequestPayload.because(".purpose is blank");
    }
    if (isBlank(purpose.coding())) {
      throw BadRequestPayload.because("Cannot determine purpose: .purpose.coding is blank");
    }
    if (purpose.coding().size() != 1) {
      throw BadRequestPayload.because(
          "Cannot determine purpose: Unexpected number of codings " + purpose.coding().size());
    }
    var purposeCode = purpose.coding().get(0).code();
    if (isBlank(purposeCode)) {
      throw BadRequestPayload.because("Purpose code is blank.");
    }
    return ContactPurpose.valueOf(purposeCode);
  }

  private Set<ExtensionHandler> quantityHandlers() {
    return Set.of(
        QuantityExtensionHandler.forDefiningUrl(
                OrganizationStructureDefinitions.PLAN_STANDARD_FILING_TIME_FRAME)
            .required(REQUIRED)
            .valueFieldNumber(InsuranceCompany.STANDARD_FTF_VALUE)
            .unitFieldNumber(InsuranceCompany.STANDARD_FTF)
            .filemanFactory(filemanFactory)
            .build());
  }

  private Set<ExtensionHandler> referenceHandlers() {
    return Set.of(
        ReferenceExtensionHandler.forDefiningUrl(OrganizationStructureDefinitions.VIA_INTERMEDIARY)
            .required(REQUIRED)
            .fieldNumber(InsuranceCompany.PAYER)
            .referenceFile(Payer.FILE_NUMBER)
            .toCoordinates(RecordCoordinates::fromString)
            .filemanFactory(filemanFactory)
            .build());
  }

  private Set<WriteableFilemanValue> rxClaimsContact(Contact contact) {
    Set<WriteableFilemanValue> contactDetails = new HashSet<>();
    companyName(contact, InsuranceCompany.CLAIMS_RX_COMPANY_NAME).ifPresent(contactDetails::add);
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
            InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_1,
            InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_2,
            InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_3,
            InsuranceCompany.CLAIMS_RX_CITY,
            InsuranceCompany.CLAIMS_RX_STATE,
            InsuranceCompany.CLAIMS_RX_ZIP,
            contact.address()));
    return contactDetails;
  }

  private Set<ExtensionHandler> stringHandlers() {
    return Set.of(
        StringExtensionHandler.forDefiningUrl(OrganizationStructureDefinitions.FILING_TIME_FRAME)
            .fieldNumber(InsuranceCompany.FILING_TIME_FRAME)
            .filemanFactory(filemanFactory)
            .required(REQUIRED)
            .build());
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceCompanyFile() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    addRequiredFields(fields);
    Optional.ofNullable(organization().id())
        .map(RecordCoordinates::fromString)
        .map(filemanFactory.recordCoordinatesToPointer(InsuranceCompany.FILE_NUMBER, index(1)))
        .ifPresent(fields::add);
    return fields;
  }

  private Set<WriteableFilemanValue> verificationContact(Contact contact) {
    var phone =
        contactPointForSystem(contact.telecom(), ContactPointSystem.phone)
            .map(
                filemanFactory.toString(
                    InsuranceCompany.VERIFICATION_PHONE_NUMBER, index(1), ContactPoint::value))
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        InsuranceCompany.VERIFICATION_PHONE_NUMBER,
                        "Unable to populate phone for VERIFY contact."));
    return Set.of(phone);
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
