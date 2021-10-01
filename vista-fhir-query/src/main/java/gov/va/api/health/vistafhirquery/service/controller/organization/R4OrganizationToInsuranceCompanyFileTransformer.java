package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4OrganizationToInsuranceCompanyFileTransformer {
  static final Map<Boolean, String> YES_NO = Map.of(true, "YES", false, "NO");

  @NonNull Organization organization;

  @Builder
  R4OrganizationToInsuranceCompanyFileTransformer(Organization organization) {
    this.organization = organization;
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

  Optional<Coding> codingForSystem(List<Coding> codings, String system) {
    if (isBlank(codings)) {
      return Optional.empty();
    }
    return codings.stream().filter(c -> system.equals(c.system())).findFirst();
  }

  WriteableFilemanValue companyName(
      Organization.Contact contact, String fieldNumber, String contactType) {
    Extension companyNameExtension =
        extensionForSystem(
                contact.extension(),
                "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
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

  WriteableFilemanValue extensionCodeableConcept(
      String fieldName, String fieldNumber, String extensionSystem, String codingSystem) {
    Extension extension =
        extensionForSystem(organization.extension(), extensionSystem)
            .orElseThrow(
                () -> BadRequestPayload.because(fieldNumber, fieldName + " extension is null"));
    CodeableConcept extensionCodeableConcept = extension.valueCodeableConcept();
    if (isBlank(extensionCodeableConcept)) {
      throw BadRequestPayload.because(
          fieldNumber, fieldName + " extension.codeableConcept is null");
    }
    Coding coding =
        codingForSystem(extensionCodeableConcept.coding(), codingSystem)
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        fieldNumber, fieldName + " extension.codeableConcept.coding is null"));
    return insuranceCompanyCoordinatesOrDie(fieldNumber, 1, coding.code(), fieldName);
  }

  Optional<Extension> extensionForSystem(List<Extension> extensions, String system) {
    if (isBlank(extensions)) {
      return Optional.empty();
    }
    return extensions.stream().filter(e -> system.equals(e.url())).findFirst();
  }

  WriteableFilemanValue extensionYesNoBoolean(String fieldName, String fieldNumber, String system) {
    Extension extension =
        extensionForSystem(organization.extension(), system)
            .orElseThrow(
                () -> BadRequestPayload.because(fieldNumber, fieldName + " extension is null"));
    if (extension.valueBoolean() == null) {
      throw BadRequestPayload.because(fieldNumber, fieldName + " extension.valueBoolean is null");
    }
    return insuranceCompanyCoordinatesOrDie(
        fieldNumber, 1, YES_NO.get(extension.valueBoolean()), fieldName);
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

  private Set<WriteableFilemanValue> standardFtf() {
    Extension standardFtfExtension =
        extensionForSystem(
                organization.extension(),
                "http://va.gov/fhir/StructureDefinition/organization-planStandardFilingTimeFrame")
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        InsuranceCompany.STANDARD_FTF, "standard ftf extension is null"));
    Quantity standardFtfQuantity = standardFtfExtension.valueQuantity();
    if (isBlank(standardFtfQuantity)) {
      throw BadRequestPayload.because(
          InsuranceCompany.STANDARD_FTF, "standard ftf quantity is null");
    }
    return Set.of(
        insuranceCompanyCoordinatesOrDie(
            InsuranceCompany.STANDARD_FTF, 1, standardFtfQuantity.unit(), "standard ftf unit"),
        insuranceCompanyCoordinatesOrDie(
            InsuranceCompany.STANDARD_FTF_VALUE,
            1,
            isBlank(standardFtfQuantity.value())
                ? null
                : String.valueOf(standardFtfQuantity.value()),
            "standard ftf unit"));
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceCompanyFile() {
    // TODO: Remove Fugazi name generation https://vajira.max.gov/browse/API-10384
    var n = System.currentTimeMillis() / 1000;
    Set<WriteableFilemanValue> fields = new HashSet<>();
    fields.add(
        insuranceCompanyCoordinatesOrDie(
            InsuranceCompany.NAME, 1, organization.name() + " : " + n, "name"));
    fields.addAll(address());
    fields.addAll(contacts());
    fields.add(
        contactPoint(
            organization.telecom(),
            InsuranceCompany.FAX_NUMBER,
            "organization",
            ContactPoint.ContactPointSystem.fax));
    fields.add(
        extensionCodeableConcept(
            "type of coverage",
            InsuranceCompany.TYPE_OF_COVERAGE,
            "http://va.gov/fhir/StructureDefinition/organization-typeOfCoverage",
            "urn:oid:2.16.840.1.113883.3.8901.3.36.8013"));
    fields.add(
        contactPoint(
            organization.telecom(),
            InsuranceCompany.PHONE_NUMBER,
            "organization",
            ContactPoint.ContactPointSystem.phone));
    fields.addAll(standardFtf());
    fields.add(
        extensionCodeableConcept(
            "reimburse",
            InsuranceCompany.REIMBURSE_,
            "http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare",
            "urn:oid:2.16.840.1.113883.3.8901.3.1.36.1"));
    fields.add(
        extensionYesNoBoolean(
            "signature required on bill",
            InsuranceCompany.SIGNATURE_REQUIRED_ON_BILL_,
            "http://va.gov/fhir/StructureDefinition/organization-signatureRequiredOnBill"));
    fields.add(
        extensionCodeableConcept(
            "transmit electronically",
            InsuranceCompany.TRANSMIT_ELECTRONICALLY,
            "http://va.gov/fhir/StructureDefinition/organization-electronicTransmissionMode",
            "urn:oid:2.16.840.1.113883.3.8901.3.1.36.38001"));
    fields.add(
        extensionCodeableConcept(
            "electronic insurance type",
            InsuranceCompany.ELECTRONIC_INSURANCE_TYPE,
            "http://va.gov/fhir/StructureDefinition/organization-electronicInsuranceType",
            "urn:oid:2.16.840.1.113883.3.8901.3.1.36.38009"));
    fields.add(
        extensionCodeableConcept(
            "ref prov sec id req on claims",
            InsuranceCompany.REF_PROV_SEC_ID_REQ_ON_CLAIMS,
            "http://va.gov/fhir/StructureDefinition/organization-referrngProviderSecondIDTypeUB04",
            "urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001"));
    fields.add(
        extensionYesNoBoolean(
            "att/rend id bill sec id prof",
            InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_PROF,
            "http://va.gov/fhir/StructureDefinition/organization-attendingRenderingProviderSecondaryIDProfesionalRequired"));
    fields.add(
        extensionYesNoBoolean(
            "att rend id bill sec id inst",
            InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_INST,
            "http://va.gov/fhir/StructureDefinition/organization-attendingRenderingProviderSecondaryIDInstitutionalRequired"));
    fields.add(
        extensionYesNoBoolean(
            "print sec tert auto claims",
            InsuranceCompany.PRINT_SEC_TERT_AUTO_CLAIMS_,
            "http://va.gov/fhir/StructureDefinition/organization-printSecTertAutoClaimsLocally"));
    fields.add(
        extensionYesNoBoolean(
            "print sec med claims w/o mra",
            InsuranceCompany.PRINT_SEC_MED_CLAIMS_W_O_MRA_,
            "http://va.gov/fhir/StructureDefinition/organization-printSecMedClaimsWOMRALocally"));
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
