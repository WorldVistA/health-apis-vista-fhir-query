package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.emptyToNull;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGateway.allFieldsOfSubfile;
import static java.util.Collections.emptyList;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.ExtensionFactory;
import gov.va.api.health.vistafhirquery.service.controller.FileLookup;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.N277EdiIdNumber;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class R4OrganizationInsuranceCompanyTransformer {
  // The following list can be generated using:
  // grep InsuranceCompany R4OrganizationTransformer.java \
  // | sed 's/.*\(InsuranceCompany\.[A-Z0-9_]\+\).*/\1,/' \
  // | grep -vE '(import|FILE_NUMBER)' \
  // | sort -u
  /** The insurance company fields needed by the transformer. */
  public static final List<String> VISTA_FIELDS =
      List.of(
          InsuranceCompany.ALLOW_MULTIPLE_BEDSECTIONS,
          InsuranceCompany.AMBULATORY_SURG_REV_CODE,
          InsuranceCompany.ANOTHER_CO_PROC_DENT_CLAIMS_,
          InsuranceCompany.ANOTHER_CO_PROCESS_APPEALS_,
          InsuranceCompany.ANOTHER_CO_PROCESS_INQUIRIES_,
          InsuranceCompany.ANOTHER_CO_PROCESS_IP_CLAIMS_,
          InsuranceCompany.ANOTHER_CO_PROCESS_OP_CLAIMS_,
          InsuranceCompany.ANOTHER_CO_PROCESS_PRECERTS_,
          InsuranceCompany.ANOTHER_CO_PROCESS_RX_CLAIMS_,
          InsuranceCompany.APPEALS_ADDRESS_CITY,
          InsuranceCompany.APPEALS_ADDRESS_ST_LINE_1_,
          InsuranceCompany.APPEALS_ADDRESS_ST_LINE_2_,
          InsuranceCompany.APPEALS_ADDRESS_ST_LINE_3_,
          InsuranceCompany.APPEALS_ADDRESS_STATE,
          InsuranceCompany.APPEALS_ADDRESS_ZIP,
          InsuranceCompany.APPEALS_COMPANY_NAME,
          InsuranceCompany.APPEALS_FAX,
          InsuranceCompany.APPEALS_PHONE_NUMBER,
          InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_INST,
          InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_PROF,
          InsuranceCompany.BILLING_COMPANY_NAME,
          InsuranceCompany.BILLING_PHONE_NUMBER,
          InsuranceCompany.BIN_NUMBER,
          InsuranceCompany.CITY,
          InsuranceCompany.CLAIMS_DENTAL_COMPANY_NAME,
          InsuranceCompany.CLAIMS_DENTAL_FAX,
          InsuranceCompany.CLAIMS_DENTAL_PHONE_NUMBER,
          InsuranceCompany.CLAIMS_DENTAL_PROCESS_CITY,
          InsuranceCompany.CLAIMS_DENTAL_PROCESS_STATE,
          InsuranceCompany.CLAIMS_DENTAL_PROCESS_ZIP,
          InsuranceCompany.CLAIMS_DENTAL_STREET_ADDR_1,
          InsuranceCompany.CLAIMS_DENTAL_STREET_ADDR_2,
          InsuranceCompany.CLAIMS_INPT_COMPANY_NAME,
          InsuranceCompany.CLAIMS_INPT_FAX,
          InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER,
          InsuranceCompany.CLAIMS_INPT_PROCESS_CITY,
          InsuranceCompany.CLAIMS_INPT_PROCESS_STATE,
          InsuranceCompany.CLAIMS_INPT_PROCESS_ZIP,
          InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_1,
          InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_2,
          InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_3,
          InsuranceCompany.CLAIMS_OPT_COMPANY_NAME,
          InsuranceCompany.CLAIMS_OPT_FAX,
          InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER,
          InsuranceCompany.CLAIMS_OPT_PROCESS_CITY,
          InsuranceCompany.CLAIMS_OPT_PROCESS_STATE,
          InsuranceCompany.CLAIMS_OPT_PROCESS_ZIP,
          InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_1,
          InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_2,
          InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_3,
          InsuranceCompany.CLAIMS_RX_CITY,
          InsuranceCompany.CLAIMS_RX_COMPANY_NAME,
          InsuranceCompany.CLAIMS_RX_FAX,
          InsuranceCompany.CLAIMS_RX_PHONE_NUMBER,
          InsuranceCompany.CLAIMS_RX_STATE,
          InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_1,
          InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_2,
          InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_3,
          InsuranceCompany.CLAIMS_RX_ZIP,
          InsuranceCompany.ELECTRONIC_INSURANCE_TYPE,
          InsuranceCompany.EDI_INST_SECONDARY_ID_1_,
          InsuranceCompany.EDI_INST_SECONDARY_ID_QUAL_1_,
          InsuranceCompany.EDI_INST_SECONDARY_ID_2_,
          InsuranceCompany.EDI_INST_SECONDARY_ID_QUAL_2_,
          InsuranceCompany.EDI_ID_NUMBER_DENTAL,
          InsuranceCompany.EDI_ID_NUMBER_INST,
          InsuranceCompany.EDI_ID_NUMBER_PROF,
          InsuranceCompany.EDI_PROF_SECONDARY_ID_1_,
          InsuranceCompany.EDI_PROF_SECONDARY_ID_QUAL_1_,
          InsuranceCompany.EDI_PROF_SECONDARY_ID_2_,
          InsuranceCompany.EDI_PROF_SECONDARY_ID_QUAL_2_,
          InsuranceCompany.FAX_NUMBER,
          InsuranceCompany.FILING_TIME_FRAME,
          InsuranceCompany.INACTIVE,
          InsuranceCompany.INQUIRY_ADDRESS_CITY,
          InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_1_,
          InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_2_,
          InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_3_,
          InsuranceCompany.INQUIRY_ADDRESS_STATE,
          InsuranceCompany.INQUIRY_ADDRESS_ZIP_CODE,
          InsuranceCompany.INQUIRY_COMPANY_NAME,
          InsuranceCompany.INQUIRY_FAX,
          InsuranceCompany.INQUIRY_PHONE_NUMBER,
          allFieldsOfSubfile(InsuranceCompany.N277EDI_ID_NUMBER).get(0),
          InsuranceCompany.NAME,
          InsuranceCompany.ONE_OPT_VISIT_ON_BILL_ONLY,
          InsuranceCompany.PAYER,
          InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_1500,
          InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_UB,
          InsuranceCompany.PHONE_NUMBER,
          InsuranceCompany.PRECERT_COMPANY_NAME,
          InsuranceCompany.PRINT_SEC_MED_CLAIMS_W_O_MRA_,
          InsuranceCompany.PRINT_SEC_TERT_AUTO_CLAIMS_,
          InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER,
          InsuranceCompany.PRESCRIPTION_REFILL_REV_CODE,
          InsuranceCompany.REIMBURSE_,
          InsuranceCompany.REF_PROV_SEC_ID_DEF_CMS_1500,
          InsuranceCompany.REF_PROV_SEC_ID_REQ_ON_CLAIMS,
          InsuranceCompany.SIGNATURE_REQUIRED_ON_BILL_,
          InsuranceCompany.STANDARD_FTF,
          InsuranceCompany.STANDARD_FTF_VALUE,
          InsuranceCompany.STATE,
          InsuranceCompany.STREET_ADDRESS_LINE_1_,
          InsuranceCompany.STREET_ADDRESS_LINE_2_,
          InsuranceCompany.STREET_ADDRESS_LINE_3_,
          InsuranceCompany.TRANSMIT_ELECTRONICALLY,
          InsuranceCompany.TYPE_OF_COVERAGE,
          InsuranceCompany.VERIFICATION_PHONE_NUMBER,
          InsuranceCompany.ZIP_CODE);

  static final Map<String, Boolean> YES_NO = Map.of("1", true, "0", false);
  private final FileLookup fileLookup;
  @Getter private final String site;
  private final Map.Entry<String, LhsLighthouseRpcGatewayResponse.Results> rpcResults;

  /** R4OrganizationInsuranceCompanyTransformer builder. */
  @Builder
  public R4OrganizationInsuranceCompanyTransformer(
      @NonNull Map.Entry<String, LhsLighthouseRpcGatewayResponse.Results> rpcResults) {
    this.site = rpcResults.getKey();
    this.rpcResults = rpcResults;
    this.fileLookup = FileLookup.of(rpcResults.getValue());
  }

  private Address address(
      String streetAddressLine1,
      String streetAddressLine2,
      String streetAddressLine3,
      String city,
      String state,
      String zipCode) {
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
        .build();
  }

  private Organization.Contact appealsContact(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        entry.internal(InsuranceCompany.APPEALS_ADDRESS_ST_LINE_1_).orElse(null),
        entry.internal(InsuranceCompany.APPEALS_ADDRESS_ST_LINE_2_).orElse(null),
        entry.internal(InsuranceCompany.APPEALS_ADDRESS_ST_LINE_3_).orElse(null),
        entry.internal(InsuranceCompany.APPEALS_ADDRESS_CITY).orElse(null),
        entry.external(InsuranceCompany.APPEALS_ADDRESS_STATE).orElse(null),
        entry.internal(InsuranceCompany.APPEALS_ADDRESS_ZIP).orElse(null),
        "APPEAL",
        entry.internal(InsuranceCompany.APPEALS_PHONE_NUMBER).orElse(null),
        entry.internal(InsuranceCompany.APPEALS_FAX).orElse(null),
        entry.internal(InsuranceCompany.APPEALS_COMPANY_NAME).orElse(null));
  }

  private Organization.Contact billingContact(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        null,
        null,
        null,
        null,
        null,
        null,
        "BILL",
        entry.internal(InsuranceCompany.BILLING_PHONE_NUMBER).orElse(null),
        null,
        entry.internal(InsuranceCompany.BILLING_COMPANY_NAME).orElse(null));
  }

  private Organization.Contact claimsDentalContact(
      LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        entry.internal(InsuranceCompany.CLAIMS_DENTAL_STREET_ADDR_1).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_DENTAL_STREET_ADDR_2).orElse(null),
        null,
        entry.internal(InsuranceCompany.CLAIMS_DENTAL_PROCESS_CITY).orElse(null),
        entry.external(InsuranceCompany.CLAIMS_DENTAL_PROCESS_STATE).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_DENTAL_PROCESS_ZIP).orElse(null),
        "DENTALCLAIMS",
        entry.internal(InsuranceCompany.CLAIMS_DENTAL_PHONE_NUMBER).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_DENTAL_FAX).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_DENTAL_COMPANY_NAME).orElse(null));
  }

  private Organization.Contact claimsInptContact(
      LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        entry.internal(InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_1).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_2).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_INPT_STREET_ADDRESS_3).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_INPT_PROCESS_CITY).orElse(null),
        entry.external(InsuranceCompany.CLAIMS_INPT_PROCESS_STATE).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_INPT_PROCESS_ZIP).orElse(null),
        "INPTCLAIMS",
        entry.internal(InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_INPT_FAX).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_INPT_COMPANY_NAME).orElse(null));
  }

  private Organization.Contact claimsOptContact(
      LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        entry.internal(InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_1).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_2).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_OPT_STREET_ADDRESS_3).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_OPT_PROCESS_CITY).orElse(null),
        entry.external(InsuranceCompany.CLAIMS_OPT_PROCESS_STATE).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_OPT_PROCESS_ZIP).orElse(null),
        "OUTPTCLAIMS",
        entry.internal(InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_OPT_FAX).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_OPT_COMPANY_NAME).orElse(null));
  }

  private Organization.Contact claimsRxContact(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        entry.internal(InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_1).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_2).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_RX_STREET_ADDRESS_3).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_RX_CITY).orElse(null),
        entry.external(InsuranceCompany.CLAIMS_RX_STATE).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_RX_ZIP).orElse(null),
        "RXCLAIMS",
        entry.internal(InsuranceCompany.CLAIMS_RX_PHONE_NUMBER).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_RX_FAX).orElse(null),
        entry.internal(InsuranceCompany.CLAIMS_RX_COMPANY_NAME).orElse(null));
  }

  private List<Address> collectAddress(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return Collections.singletonList(
        address(
            entry.internal(InsuranceCompany.STREET_ADDRESS_LINE_1_).orElse(null),
            entry.internal(InsuranceCompany.STREET_ADDRESS_LINE_2_).orElse(null),
            entry.internal(InsuranceCompany.STREET_ADDRESS_LINE_3_).orElse(null),
            entry.internal(InsuranceCompany.CITY).orElse(null),
            entry.external(InsuranceCompany.STATE).orElse(null),
            entry.internal(InsuranceCompany.ZIP_CODE).orElse(null)));
  }

  private List<Extension> companyNameExtension(String companyName) {
    if (isBlank(companyName)) {
      return emptyList();
    }
    return Extension.builder()
        .valueReference(Reference.builder().display(companyName).build())
        .url(OrganizationStructureDefinitions.VIA_INTERMEDIARY)
        .build()
        .asList();
  }

  private Organization.Contact contact(
      String streetAddressLine1,
      String streetAddressLine2,
      String streetAddressLine3,
      String city,
      String state,
      String zipCode,
      String purpose,
      String phone,
      String fax,
      String companyName) {
    if (allBlank(
        streetAddressLine1,
        streetAddressLine2,
        streetAddressLine3,
        city,
        state,
        zipCode,
        purpose,
        phone,
        fax,
        companyName)) {
      return null;
    }
    return Organization.Contact.builder()
        .address(
            address(
                streetAddressLine1, streetAddressLine2, streetAddressLine3, city, state, zipCode))
        .telecom(emptyToNull(contactTelecom(phone, fax)))
        .extension(emptyToNull(companyNameExtension(companyName)))
        .purpose(purposeOrNull(purpose))
        .build();
  }

  private List<ContactPoint> contactTelecom(String phone, String fax) {
    List<ContactPoint> telecoms = new ArrayList<>();
    if (!isBlank(phone)) {
      telecoms.add(
          ContactPoint.builder()
              .value(phone)
              .system(ContactPoint.ContactPointSystem.phone)
              .build());
    }
    if (!isBlank(fax)) {
      telecoms.add(
          ContactPoint.builder().value(fax).system(ContactPoint.ContactPointSystem.fax).build());
    }
    return telecoms;
  }

  private List<Organization.Contact> contacts(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return Stream.of(
            appealsContact(entry),
            billingContact(entry),
            claimsDentalContact(entry),
            claimsInptContact(entry),
            claimsOptContact(entry),
            claimsRxContact(entry),
            inquiryContact(entry),
            precertificationContact(entry),
            verificationContact(entry))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private List<Extension> extensions(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    ExtensionFactory extensions = ExtensionFactory.of(entry, YES_NO);
    return Stream.of(
            extensions.ofYesNoBoolean(
                InsuranceCompany.ALLOW_MULTIPLE_BEDSECTIONS,
                OrganizationStructureDefinitions.ALLOW_MULTIPLE_BEDSECTIONS),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ONE_OPT_VISIT_ON_BILL_ONLY,
                OrganizationStructureDefinitions.ONE_OUTPAT_VISIT_ON_BILL_ONLY),
            extensions.ofCodeableConceptFromInternalValue(
                InsuranceCompany.AMBULATORY_SURG_REV_CODE,
                OrganizationStructureDefinitions.AMBULATORY_SURGERY_REVENUE_CODE_URN_OID,
                OrganizationStructureDefinitions.AMBULATORY_SURGERY_REVENUE_CODE),
            extensions.ofString(
                InsuranceCompany.FILING_TIME_FRAME,
                OrganizationStructureDefinitions.FILING_TIME_FRAME),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ANOTHER_CO_PROCESS_IP_CLAIMS_,
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_INPAT_CLAIMS),
            extensions.ofCodeableConceptFromExternalValue(
                InsuranceCompany.TYPE_OF_COVERAGE,
                OrganizationStructureDefinitions.TYPE_OF_COVERAGE_URN_OID,
                OrganizationStructureDefinitions.TYPE_OF_COVERAGE),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ANOTHER_CO_PROCESS_APPEALS_,
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_APPEALS),
            extensions.ofCodeableConceptFromInternalValue(
                InsuranceCompany.PRESCRIPTION_REFILL_REV_CODE,
                OrganizationStructureDefinitions.PRESCRIPTION_REVENUE_CODE_URN_OID,
                OrganizationStructureDefinitions.PRESCRIPTION_REVENUE_CODE),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ANOTHER_CO_PROCESS_INQUIRIES_,
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_INQUIRIES),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ANOTHER_CO_PROCESS_OP_CLAIMS_,
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_OUTPAT_CLAIMS),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ANOTHER_CO_PROCESS_PRECERTS_,
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_PRECERT),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ANOTHER_CO_PROCESS_RX_CLAIMS_,
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_RX_CLAIMS),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ANOTHER_CO_PROC_DENT_CLAIMS_,
                OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_DENTAL_CLAIMS),
            extensions.ofQuantity(
                InsuranceCompany.STANDARD_FTF_VALUE,
                entry.external(InsuranceCompany.STANDARD_FTF).orElse(null),
                OrganizationStructureDefinitions.PLAN_STANDARD_FILING_TIME_FRAME_URN_OID,
                OrganizationStructureDefinitions.PLAN_STANDARD_FILING_TIME_FRAME),
            extensions.ofCodeableConceptFromExternalValue(
                InsuranceCompany.REIMBURSE_,
                OrganizationStructureDefinitions.WILL_REIMBURSE_FOR_CARE_URN_OID,
                OrganizationStructureDefinitions.WILL_REIMBURSE_FOR_CARE),
            extensions.ofYesNoBoolean(
                InsuranceCompany.SIGNATURE_REQUIRED_ON_BILL_,
                OrganizationStructureDefinitions.SIGNATURE_REQUIRED_ON_BILL),
            extensions.ofCodeableConceptFromExternalValue(
                InsuranceCompany.TRANSMIT_ELECTRONICALLY,
                OrganizationStructureDefinitions.ELECTRONIC_TRANSMISSION_MODE_URN_OID,
                OrganizationStructureDefinitions.ELECTRONIC_TRANSMISSION_MODE),
            extensions.ofCodeableConceptFromExternalValue(
                InsuranceCompany.ELECTRONIC_INSURANCE_TYPE,
                OrganizationStructureDefinitions.ELECTRONIC_INSURANCE_TYPE_URN_OID,
                OrganizationStructureDefinitions.ELECTRONIC_INSURANCE_TYPE),
            extensions.ofReference(
                "Organization", payerId(entry), OrganizationStructureDefinitions.VIA_INTERMEDIARY),
            extensions.ofCodeableConceptFromExternalValue(
                InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_1500,
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID,
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500),
            extensions.ofCodeableConceptFromExternalValue(
                InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_UB,
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID,
                OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04),
            extensions.ofCodeableConceptFromExternalValue(
                InsuranceCompany.REF_PROV_SEC_ID_DEF_CMS_1500,
                OrganizationStructureDefinitions.REFERRING_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID,
                OrganizationStructureDefinitions.REFERRING_PROVIDER_SECOND_IDTYPE_CMS_1500),
            extensions.ofCodeableConceptFromExternalValue(
                InsuranceCompany.REF_PROV_SEC_ID_REQ_ON_CLAIMS,
                OrganizationStructureDefinitions.REFERRING_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID,
                OrganizationStructureDefinitions.REFERRING_PROVIDER_SECOND_IDTYPE_UB_04),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_PROF,
                OrganizationStructureDefinitions
                    .ATTENDING_RENDERING_PROVIDER_SECONDARY_IDPROFESIONAL_REQUIRED),
            extensions.ofYesNoBoolean(
                InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_INST,
                OrganizationStructureDefinitions
                    .ATTENDING_RENDERING_PROVIDER_SECONDARY_IDINSTITUTIONAL_REQUIRED),
            extensions.ofYesNoBoolean(
                InsuranceCompany.PRINT_SEC_TERT_AUTO_CLAIMS_,
                OrganizationStructureDefinitions.PRINT_SEC_TERT_AUTO_CLAIMS_LOCALLY),
            extensions.ofYesNoBoolean(
                InsuranceCompany.PRINT_SEC_MED_CLAIMS_W_O_MRA_,
                OrganizationStructureDefinitions.PRINT_SEC_MED_CLAIMS_WOMRALOCALLY))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private Identifier identifier(Optional<String> value, String code) {
    if (value.isEmpty()) {
      return null;
    }
    return Identifier.builder()
        .value(value.get())
        .type(
            CodeableConcept.builder().coding(List.of(Coding.builder().code(code).build())).build())
        .build();
  }

  private Identifier identifierSlice(
      Optional<String> maybeValue, Optional<String> maybeCode, String system) {
    if (isBlank(maybeValue) || isBlank(maybeCode)) {
      return null;
    }
    return Identifier.builder()
        .value(maybeValue.get())
        .type(
            CodeableConcept.builder()
                .coding(Coding.builder().code(maybeCode.get()).system(system).build().asList())
                .build())
        .build();
  }

  private List<Identifier> identifiers(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return Stream.concat(
            Stream.of(
                identifier(
                    entry.internal(InsuranceCompany.EDI_ID_NUMBER_PROF),
                    OrganizationStructureDefinitions.EDI_ID_NUMBER_PROF_CODE),
                identifier(
                    entry.internal(InsuranceCompany.EDI_ID_NUMBER_INST),
                    OrganizationStructureDefinitions.EDI_ID_NUMBER_INST_CODE),
                identifier(
                    entry.internal(InsuranceCompany.BIN_NUMBER),
                    OrganizationStructureDefinitions.BIN_NUMBER_CODE),
                identifier(
                    entry.internal(InsuranceCompany.EDI_ID_NUMBER_DENTAL),
                    OrganizationStructureDefinitions.EDI_ID_NUMBER_DENTAL_CODE),
                identifierSlice(
                    entry.internal(InsuranceCompany.EDI_INST_SECONDARY_ID_1_),
                    entry.external(InsuranceCompany.EDI_INST_SECONDARY_ID_QUAL_1_),
                    OrganizationStructureDefinitions.EDI_INST_SECONDARY_ID_QUAL_1),
                identifierSlice(
                    entry.internal(InsuranceCompany.EDI_INST_SECONDARY_ID_2_),
                    entry.external(InsuranceCompany.EDI_INST_SECONDARY_ID_QUAL_2_),
                    OrganizationStructureDefinitions.EDI_INST_SECONDARY_ID_QUAL_2),
                identifierSlice(
                    entry.internal(InsuranceCompany.EDI_PROF_SECONDARY_ID_1_),
                    entry.external(InsuranceCompany.EDI_PROF_SECONDARY_ID_QUAL_1_),
                    OrganizationStructureDefinitions.EDI_PROF_SECONDARY_ID_QUAL_1),
                identifierSlice(
                    entry.internal(InsuranceCompany.EDI_PROF_SECONDARY_ID_2_),
                    entry.external(InsuranceCompany.EDI_PROF_SECONDARY_ID_QUAL_2_),
                    OrganizationStructureDefinitions.EDI_PROF_SECONDARY_ID_QUAL_2)),
            n277EdiIdNumber(entry).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private Organization.Contact inquiryContact(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        entry.internal(InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_1_).orElse(null),
        entry.internal(InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_2_).orElse(null),
        entry.internal(InsuranceCompany.INQUIRY_ADDRESS_ST_LINE_3_).orElse(null),
        entry.internal(InsuranceCompany.INQUIRY_ADDRESS_CITY).orElse(null),
        entry.external(InsuranceCompany.INQUIRY_ADDRESS_STATE).orElse(null),
        entry.internal(InsuranceCompany.INQUIRY_ADDRESS_ZIP_CODE).orElse(null),
        "INQUIRY",
        entry.internal(InsuranceCompany.INQUIRY_PHONE_NUMBER).orElse(null),
        entry.internal(InsuranceCompany.INQUIRY_FAX).orElse(null),
        entry.internal(InsuranceCompany.INQUIRY_COMPANY_NAME).orElse(null));
  }

  private List<CodeableConcept> insuranceCompanyType() {
    return List.of(
        asCodeableConcept(
            Coding.builder()
                .code("ins")
                .display("Insurance Company")
                .system("http://hl7.org/fhir/ValueSet/organization-type")
                .build()));
  }

  private Set<Identifier> n277EdiIdNumber(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return fileLookup
        .findByFileNumberAndParentIen(N277EdiIdNumber.FILE_NUMBER, entry.ien())
        .stream()
        .map(
            e ->
                identifier(
                    e.external(N277EdiIdNumber.N277EDI_ID_NUMBER),
                    OrganizationStructureDefinitions.N277_EDI_ID_NUMBER_CODE))
        .collect(Collectors.toSet());
  }

  private String payerId(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    var value = entry.internal(InsuranceCompany.PAYER);
    if (value.isEmpty()) {
      return null;
    }
    return RecordCoordinates.builder()
        .site(site())
        .file(Payer.FILE_NUMBER)
        .ien(value.get())
        .build()
        .toString();
  }

  private Organization.Contact precertificationContact(
      LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        null,
        null,
        null,
        null,
        null,
        null,
        "PRECERT",
        entry.internal(InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER).orElse(null),
        null,
        entry.internal(InsuranceCompany.PRECERT_COMPANY_NAME).orElse(null));
  }

  private CodeableConcept purposeOrNull(String purpose) {
    if (isBlank(purpose)) {
      return null;
    }
    return asCodeableConcept(
        Coding.builder()
            .code(purpose)
            .display(purpose)
            .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
            .build());
  }

  /** Transform an RPC response to fhir. */
  public Stream<Organization> toFhir() {
    return rpcResults.getValue().results().stream()
        .filter(Objects::nonNull)
        .filter(r -> InsuranceCompany.FILE_NUMBER.equals(r.file()))
        .map(this::toOrganization)
        .filter(Objects::nonNull);
  }

  private Organization toOrganization(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    if (entry == null || isBlank(entry.fields())) {
      return null;
    }
    return Organization.builder()
        .meta(Meta.builder().source(site()).build())
        .id(
            RecordCoordinates.builder()
                .site(site())
                .file(InsuranceCompany.FILE_NUMBER)
                .ien(entry.ien())
                .build()
                .toString())
        .extension(extensions(entry))
        .identifier(emptyToNull(identifiers(entry)))
        .active(entry.internal(InsuranceCompany.INACTIVE, YES_NO).map(value -> !value).orElse(true))
        .name(entry.internal(InsuranceCompany.NAME).orElse(null))
        .type(insuranceCompanyType())
        .address(collectAddress(entry))
        .contact(contacts(entry))
        .telecom(
            emptyToNull(
                contactTelecom(
                    entry.internal(InsuranceCompany.PHONE_NUMBER).orElse(null),
                    entry.internal(InsuranceCompany.FAX_NUMBER).orElse(null))))
        .build();
  }

  private Organization.Contact verificationContact(
      LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return contact(
        null,
        null,
        null,
        null,
        null,
        null,
        "VERIFY",
        entry.internal(InsuranceCompany.VERIFICATION_PHONE_NUMBER).orElse(null),
        null,
        null);
  }
}
