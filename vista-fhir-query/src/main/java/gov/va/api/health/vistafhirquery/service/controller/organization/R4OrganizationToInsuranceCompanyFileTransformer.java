package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.HashSet;
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

  WriteableFilemanValue insuranceCompanyCoordinatesOrBadRequestPayload(
      String field, Integer index, String value, String fieldName) {
    if (isBlank(field)) {
      throw ResourceExceptions.BadRequestPayload.because(field, fieldName + " is null");
    }
    return WriteableFilemanValue.builder()
        .file(InsuranceCompany.FILE_NUMBER)
        .field(field)
        .index(index)
        .value(value)
        .build();
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceCompanyFile() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.NAME, 1, "SHANKS OF FL", "name"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.CITY, 1, "SHANK CITY", "city"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.STATE, 1, "SHANKTICUT", "state"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.STREET_ADDRESS_LINE_1_, 1, "SHANKSVILLE LINE 1", "address line 1"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.STREET_ADDRESS_LINE_2_, 1, "SHANKSVILLE LINE 2", "address line 2"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.STREET_ADDRESS_LINE_3_, 1, "SHANKSVILLE LINE 3", "address line 3"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.ZIP_CODE, 1, "SHANKZIP", "zipcode"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.BILLING_COMPANY_NAME, 1, "SHANK-BILLING", "billing company name"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.FAX_NUMBER, 1, "SHANKFAX", "fax number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.TYPE_OF_COVERAGE, 1, "SHANK INSURANCE", "type of coverage"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.PHONE_NUMBER, 1, "1-800-SHANKTO", "phone number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.BILLING_PHONE_NUMBER,
            1,
            "1-800-SHANK-BILLING",
            "billing phone number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER,
            1,
            "1-800-SHANK-PRECERT",
            "precertification phone number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.VERIFICATION_PHONE_NUMBER,
            1,
            "1-800-SHANK-VERIFICATION",
            "verification phone number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER,
            1,
            "1-800-SHANK-CLAIMS-INPT",
            "claims inpt phone number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER,
            1,
            "1-800-SHANK-CLAIMS-OPT",
            "claims opt phone number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.APPEALS_PHONE_NUMBER,
            1,
            "1-800-SHANK-APPEALS",
            "appeals phone number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.INQUIRY_PHONE_NUMBER,
            1,
            "1-800-SHANK-INQUIRY",
            "inquiry phone number"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.STANDARD_FTF, 1, "DAYS", "standard ftf"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.STANDARD_FTF_VALUE, 1, "365", "standard ftf value"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.REIMBURSE_, 1, "Y", "reimburse"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.SIGNATURE_REQUIRED_ON_BILL_, 1, "1", "signature required on bill"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.TRANSMIT_ELECTRONICALLY, 1, "2", "transmit electronically"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.EDI_ID_NUMBER_PROF, 1, "55555", "edi id number prof"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.EDI_ID_NUMBER_INST, 1, "55555", "edi id number inst"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.ELECTRONIC_INSURANCE_TYPE,
            1,
            "ELECTRONIC INSHANKANCE",
            "electronic insurance type"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.PAYER, 1, "SHANK PAYER", "payer"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.SECONDARY_ID_REQUIREMENTS,
            1,
            "SHANKONDARY ID REQUIREMENTS",
            "secondary id requirements"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.REF_PROV_SEC_ID_REQ_ON_CLAIMS,
            1,
            "0",
            "ref prov sec id req on claims"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_PROF, 1, "0", "att rend id bill sec id prof"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_INST, 1, "0", "att rend id bill sec id inst"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.PRINT_SEC_TERT_AUTO_CLAIMS_, 1, "0", "print sec tert auto claims"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.PRINT_SEC_MED_CLAIMS_W_O_MRA_,
            1,
            "0",
            "print sec med claims w o mra"));
    fields.add(
        insuranceCompanyCoordinatesOrBadRequestPayload(
            InsuranceCompany.N277EDI_ID_NUMBER, 1, "22-7777777", "n277edi id number"));
    return fields;
  }
}
