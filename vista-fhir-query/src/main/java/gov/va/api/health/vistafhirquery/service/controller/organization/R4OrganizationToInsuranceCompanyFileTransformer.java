package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.OPTIONAL;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Organization;
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
    fields.addAll(address());
    fields.addAll(contacts());
    fields.add(
        contactPoint(
            organization.telecom(),
            InsuranceCompany.FAX_NUMBER,
            "organization",
            ContactPoint.ContactPointSystem.fax));
    fields.add(
        contactPoint(
            organization.telecom(),
            InsuranceCompany.PHONE_NUMBER,
            "organization",
            ContactPoint.ContactPointSystem.phone));
    fields.addAll(extensionProcessor.process(organization.extension()));
    fields.add(
        identifier(
            "edi id number inst",
            InsuranceCompany.EDI_ID_NUMBER_INST,
            OrganizationStructureDefinitions.EDI_ID_NUMBER_INST_CODE));
    fields.add(
        identifier(
            "edi id number prof",
            InsuranceCompany.EDI_ID_NUMBER_PROF,
            OrganizationStructureDefinitions.EDI_ID_NUMBER_PROF_CODE));
    fields.add(
        identifier(
            "bin number",
            InsuranceCompany.BIN_NUMBER,
            OrganizationStructureDefinitions.BIN_NUMBER_CODE));
  }

  private Set<WriteableFilemanValue> address() {
    if (isBlank(organization.address())) {
      throw BadRequestPayload.because(
          InsuranceCompany.STREET_ADDRESS_LINE_1_, "address is empty or null");
    }
    /* Address is a list but vista expects a single address. */
    Address firstAddress = organization.address().get(0);
    List<String> lines = firstAddress.line();
    if (isBlank(lines) && lines.size() >= 3) {
      throw BadRequestPayload.because(
          InsuranceCompany.STREET_ADDRESS_LINE_1_, "one or more address lines are null");
    }
    return Set.of(
        insuranceCompanyCoordinatesOrDie(
            InsuranceCompany.STREET_ADDRESS_LINE_1_, 1, lines.get(0), "address line 1"),
        insuranceCompanyCoordinatesOrDie(
            InsuranceCompany.STREET_ADDRESS_LINE_2_, 1, lines.get(1), "address line 2"),
        insuranceCompanyCoordinatesOrDie(
            InsuranceCompany.STREET_ADDRESS_LINE_3_, 1, lines.get(2), "address line 3"),
        insuranceCompanyCoordinatesOrDie(InsuranceCompany.CITY, 1, firstAddress.city(), "city"),
        insuranceCompanyCoordinatesOrDie(InsuranceCompany.STATE, 1, firstAddress.state(), "state"),
        insuranceCompanyCoordinatesOrDie(
            InsuranceCompany.ZIP_CODE, 1, firstAddress.postalCode(), "zip code"));
  }

  private WriteableFilemanValue appealsContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie("APPEAL", InsuranceCompany.APPEALS_PHONE_NUMBER, "appeals");
    return contactPoint(
        verificationContact.telecom(),
        InsuranceCompany.APPEALS_PHONE_NUMBER,
        "appeals",
        ContactPoint.ContactPointSystem.phone);
  }

  private Set<WriteableFilemanValue> billingContact() {
    Organization.Contact billingContact =
        contactForPurposeOrDie("BILL", InsuranceCompany.BILLING_COMPANY_NAME, "billing");
    return Set.of(
        companyName(billingContact, InsuranceCompany.BILLING_COMPANY_NAME, "billing"),
        contactPoint(
            billingContact.telecom(),
            InsuranceCompany.BILLING_PHONE_NUMBER,
            "billing",
            ContactPoint.ContactPointSystem.phone));
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

  private WriteableFilemanValue claimsInptContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie(
            "INPTCLAIMS", InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER, "claims inpt");
    return contactPoint(
        verificationContact.telecom(),
        InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER,
        "claims inpt",
        ContactPoint.ContactPointSystem.phone);
  }

  private WriteableFilemanValue claimsOptContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie(
            "OUTPTCLAIMS", InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER, "claims opt");
    return contactPoint(
        verificationContact.telecom(),
        InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER,
        "claims opt",
        ContactPoint.ContactPointSystem.phone);
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

  WriteableFilemanValue companyName(
      Organization.Contact contact, String fieldNumber, String contactType) {
    Extension companyNameExtension =
        extensionForSystem(contact.extension(), OrganizationStructureDefinitions.VIA_INTERMEDIARY)
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        fieldNumber, contactType + " contact extension is null"));
    Reference billingCompanyNameReference = companyNameExtension.valueReference();
    if (isBlank(billingCompanyNameReference)) {
      throw BadRequestPayload.because(
          fieldNumber, contactType + " contact extension value reference is null");
    }
    return insuranceCompanyCoordinatesOrDie(
        fieldNumber, 1, billingCompanyNameReference.display(), contactType + "company name");
  }

  Organization.Contact contactForPurposeOrDie(
      String purpose, String fieldNumber, String contactType) {
    List<Organization.Contact> contacts = organization().contact();
    if (isBlank(contacts)) {
      throw BadRequestPayload.because(fieldNumber, contactType + " contact is null");
    }
    CodeableConcept purposeCodeableConcept =
        asCodeableConcept(
            Coding.builder()
                .code(purpose)
                .display(purpose)
                .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                .build());
    return contacts.stream()
        .filter(c -> purposeCodeableConcept.equals(c.purpose()))
        .findFirst()
        .orElseThrow(
            () -> BadRequestPayload.because(fieldNumber, contactType + " contact is null"));
  }

  WriteableFilemanValue contactPoint(
      List<ContactPoint> telecom,
      String fieldNumber,
      String contactType,
      ContactPoint.ContactPointSystem system) {
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
      List<ContactPoint> contactPoints, ContactPoint.ContactPointSystem system) {
    if (isBlank(contactPoints)) {
      return Optional.empty();
    }
    return contactPoints.stream().filter(c -> system.equals(c.system())).findFirst();
  }

  private Set<WriteableFilemanValue> contacts() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    fields.add(appealsContact());
    fields.addAll(billingContact());
    fields.add(claimsInptContact());
    fields.add(claimsOptContact());
    fields.add(inquiryContact());
    fields.add(precertContact());
    fields.add(verificationContact());
    return fields;
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

  WriteableFilemanValue identifier(String fieldName, String fieldNumber, String identifierCode) {
    Identifier identifier =
        identifierForCode(organization.identifier(), identifierCode)
            .orElseThrow(
                () -> BadRequestPayload.because(fieldNumber, fieldName + " identifier is null"));
    if (identifier.value().isBlank()) {
      throw BadRequestPayload.because(fieldNumber, fieldName + "identifier.value is null");
    }
    return insuranceCompanyCoordinatesOrDie(fieldNumber, 1, identifier.value(), fieldName);
  }

  Optional<Identifier> identifierForCode(List<Identifier> identifiers, String identifierCode) {
    if (isBlank(identifiers)) {
      return Optional.empty();
    }
    return identifiers.stream()
        .filter(
            i -> {
              if (i.type().coding() == null) {
                return false;
              }
              return i.type().coding().stream().anyMatch(c -> identifierCode.equals(c.code()));
            })
        .findFirst();
  }

  private WriteableFilemanValue inquiryContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie("INQUIRY", InsuranceCompany.INQUIRY_PHONE_NUMBER, "inquiry");
    return contactPoint(
        verificationContact.telecom(),
        InsuranceCompany.INQUIRY_PHONE_NUMBER,
        "inquiry",
        ContactPoint.ContactPointSystem.phone);
  }

  private WriteableFilemanValue insuranceCompanyCoordinatesOrDie(
      String field, Integer index, String value, String fieldName) {
    if (isBlank(value)) {
      throw BadRequestPayload.because(field, fieldName + " is null");
    }
    return WriteableFilemanValue.builder()
        .file(InsuranceCompany.FILE_NUMBER)
        .field(field)
        .index(index)
        .value(value)
        .build();
  }

  private WriteableFilemanValue precertContact() {
    Organization.Contact precertContact =
        contactForPurposeOrDie(
            "PRECERT",
            InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER,
            "precertification contact is null");
    return contactPoint(
        precertContact.telecom(),
        InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER,
        "precertification",
        ContactPoint.ContactPointSystem.phone);
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

  private WriteableFilemanValue verificationContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie("VERIFY", InsuranceCompany.VERIFICATION_PHONE_NUMBER, "verify");
    return contactPoint(
        verificationContact.telecom(),
        InsuranceCompany.VERIFICATION_PHONE_NUMBER,
        "verify",
        ContactPoint.ContactPointSystem.phone);
  }
}
