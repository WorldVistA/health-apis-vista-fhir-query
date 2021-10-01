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

  Optional<Extension> extensionForSystem(List<Extension> extensions, String system) {
    if (isBlank(extensions)) {
      return Optional.empty();
    }
    return extensions.stream().filter(e -> system.equals(e.url())).findFirst();
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
    fields.add(typeOfCoverage());
    fields.add(
        contactPoint(
            organization.telecom(),
            InsuranceCompany.PHONE_NUMBER,
            "organization",
            ContactPoint.ContactPointSystem.phone));
    fields.addAll(standardFtf());
    return fields;
  }

  private WriteableFilemanValue typeOfCoverage() {
    Extension companyNameExtension =
        extensionForSystem(
                organization.extension(),
                "http://va.gov/fhir/StructureDefinition/organization-typeOfCoverage")
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        InsuranceCompany.TYPE_OF_COVERAGE, " type of coverage extension is null"));
    CodeableConcept typeOfCoverageCodeableConcept = companyNameExtension.valueCodeableConcept();
    if (isBlank(typeOfCoverageCodeableConcept)) {
      throw BadRequestPayload.because(
          InsuranceCompany.TYPE_OF_COVERAGE, "type of coverage codeable concept is null");
    }
    Coding typeOfCoverageCoding =
        codingForSystem(
                typeOfCoverageCodeableConcept.coding(),
                "urn:oid:2.16.840.1.113883.3.8901.3.36.8013")
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        InsuranceCompany.TYPE_OF_COVERAGE, " type of coverage coding is null"));
    ;
    return insuranceCompanyCoordinatesOrDie(
        InsuranceCompany.TYPE_OF_COVERAGE, 1, typeOfCoverageCoding.code(), "type of coverage");
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
