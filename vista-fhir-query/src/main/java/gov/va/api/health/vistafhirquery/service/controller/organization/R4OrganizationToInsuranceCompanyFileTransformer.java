package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4OrganizationToInsuranceCompanyFileTransformer {
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
    return phoneNumber(verificationContact, InsuranceCompany.APPEALS_PHONE_NUMBER, "appeals");
  }

  private Set<WriteableFilemanValue> billingContact() {
    Organization.Contact billingContact =
        contactForPurposeOrDie("BILL", InsuranceCompany.BILLING_COMPANY_NAME, "billing");
    return Set.of(
        companyName(billingContact, InsuranceCompany.BILLING_COMPANY_NAME, "billing"),
        phoneNumber(billingContact, InsuranceCompany.BILLING_PHONE_NUMBER, "billing"));
  }

  private WriteableFilemanValue claimsInptContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie(
            "INPTCLAIMS", InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER, "claims inpt");
    return phoneNumber(
        verificationContact, InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER, "claims inpt");
  }

  private WriteableFilemanValue claimsOptContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie(
            "OUTPTCLAIMS", InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER, "claims opt");
    return phoneNumber(verificationContact, InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER, "claims opt");
  }

  private WriteableFilemanValue companyName(
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

  private Organization.Contact contactForPurposeOrDie(
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

  private Optional<Extension> extensionForSystem(List<Extension> extensions, String system) {
    if (isBlank(extensions)) {
      return Optional.empty();
    }
    return extensions.stream().filter(e -> system.equals(e.url())).findFirst();
  }

  private WriteableFilemanValue inquiryContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie("INQUIRY", InsuranceCompany.INQUIRY_PHONE_NUMBER, "inquiry");
    return phoneNumber(verificationContact, InsuranceCompany.INQUIRY_PHONE_NUMBER, "inquiry");
  }

  WriteableFilemanValue insuranceCompanyCoordinatesOrDie(
      String field, Integer index, String value, String fieldName) {
    if (isBlank(field)) {
      throw BadRequestPayload.because(field, fieldName + " is null");
    }
    return WriteableFilemanValue.builder()
        .file(InsuranceCompany.FILE_NUMBER)
        .field(field)
        .index(index)
        .value(value)
        .build();
  }

  private WriteableFilemanValue phoneNumber(
      Organization.Contact contact, String fieldNumber, String contactType) {
    List<ContactPoint> telecom = contact.telecom();
    if (isBlank(telecom)) {
      throw BadRequestPayload.because(fieldNumber, contactType + "telecom reference is null");
    }
    return insuranceCompanyCoordinatesOrDie(
        fieldNumber, 1, telecom.get(0).value(), contactType + " phone number");
  }

  private WriteableFilemanValue precertContact() {
    Organization.Contact precertContact =
        contactForPurposeOrDie(
            "PRECERT",
            InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER,
            "precertification contact is null");
    return phoneNumber(
        precertContact, InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER, "precertification");
  }

  /*
  private WriteableFilemanValue pointer(@NonNull String file, int index, String ien) {
    if (isBlank(ien)) {
      return null;
    }
    return WriteableFilemanValue.builder().file(file).field("ien").index(index).value(ien).build();
  }
  */

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
    return fields;
  }

  private WriteableFilemanValue verificationContact() {
    Organization.Contact verificationContact =
        contactForPurposeOrDie("VERIFY", InsuranceCompany.VERIFICATION_PHONE_NUMBER, "verify");
    return phoneNumber(verificationContact, InsuranceCompany.VERIFICATION_PHONE_NUMBER, "verify");
  }
}
