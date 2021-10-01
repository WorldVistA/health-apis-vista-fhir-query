package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toBigDecimal;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrganizationSamples {
  @SneakyThrows
  public static String json(Object o) {
    return JacksonConfig.createMapper().writeValueAsString(o);
  }

  public static LinkProperties linkProperties() {
    return LinkProperties.builder()
        .publicUrl("http://fake.com")
        .publicR4BasePath("site/{site}/r4")
        .defaultPageSize(10)
        .maxPageSize(100)
        .build();
  }

  private WriteableFilemanValue insuranceCompanyValue(String field, String value) {
    return WriteableFilemanValue.builder()
        .file(InsuranceCompany.FILE_NUMBER)
        .index(1)
        .field(field)
        .value(value)
        .build();
  }

  private WriteableFilemanValue pointerTo(String file, String ien) {
    return WriteableFilemanValue.builder().file(file).index(1).field("ien").value(ien).build();
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    public Set<WriteableFilemanValue> createApiInput() {
      return Set.of(
          insuranceCompanyValue(InsuranceCompany.NAME, "SHANKS OF FL"),
          insuranceCompanyValue(InsuranceCompany.CITY, "SHANK CITY"),
          insuranceCompanyValue(InsuranceCompany.STATE, "12"),
          insuranceCompanyValue(InsuranceCompany.STREET_ADDRESS_LINE_1_, "SHANKSVILLE LINE 1"),
          insuranceCompanyValue(InsuranceCompany.STREET_ADDRESS_LINE_2_, "SHANKSVILLE LINE 2"),
          insuranceCompanyValue(InsuranceCompany.STREET_ADDRESS_LINE_3_, "SHANKSVILLE LINE 3"),
          insuranceCompanyValue(InsuranceCompany.ZIP_CODE, "322310014"),
          insuranceCompanyValue(InsuranceCompany.BILLING_COMPANY_NAME, "SHANK-BILLING"),
          insuranceCompanyValue(InsuranceCompany.FAX_NUMBER, "SHANKFAX"),
          pointerTo("355.2", "5"),
          insuranceCompanyValue(InsuranceCompany.PHONE_NUMBER, "800-456-8888"),
          insuranceCompanyValue(InsuranceCompany.BILLING_PHONE_NUMBER, "800-123-7777"),
          insuranceCompanyValue(InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER, "800-222-9999"),
          insuranceCompanyValue(InsuranceCompany.VERIFICATION_PHONE_NUMBER, "800-333-8888"),
          insuranceCompanyValue(InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER, "800-444-7777"),
          insuranceCompanyValue(InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER, "800-555-6666"),
          insuranceCompanyValue(InsuranceCompany.APPEALS_PHONE_NUMBER, "1-800-SHANK-APPEALS"),
          insuranceCompanyValue(InsuranceCompany.INQUIRY_PHONE_NUMBER, "1-800-SHANK-INQUIRY"),
          insuranceCompanyValue(InsuranceCompany.STANDARD_FTF, "DAYS"),
          insuranceCompanyValue(InsuranceCompany.STANDARD_FTF_VALUE, "365"),
          insuranceCompanyValue(InsuranceCompany.REIMBURSE_, "WILL REIMBURSE"),
          insuranceCompanyValue(InsuranceCompany.SIGNATURE_REQUIRED_ON_BILL_, "1"),
          insuranceCompanyValue(InsuranceCompany.TRANSMIT_ELECTRONICALLY, "2"),
          insuranceCompanyValue(InsuranceCompany.EDI_ID_NUMBER_PROF, "55555"),
          insuranceCompanyValue(InsuranceCompany.EDI_ID_NUMBER_INST, "55555"),
          insuranceCompanyValue(InsuranceCompany.ELECTRONIC_INSURANCE_TYPE, "OTHER"),
          pointerTo(Payer.FILE_NUMBER, "17"),
          insuranceCompanyValue(InsuranceCompany.SECONDARY_ID_REQUIREMENTS, "NONE REQUIRED"),
          insuranceCompanyValue(InsuranceCompany.REF_PROV_SEC_ID_REQ_ON_CLAIMS, "0"),
          insuranceCompanyValue(InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_PROF, "0"),
          insuranceCompanyValue(InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_INST, "0"),
          insuranceCompanyValue(InsuranceCompany.PRINT_SEC_TERT_AUTO_CLAIMS_, "0"),
          insuranceCompanyValue(InsuranceCompany.PRINT_SEC_MED_CLAIMS_W_O_MRA_, "0"),
          insuranceCompanyValue(InsuranceCompany.N277EDI_ID_NUMBER, "22-7777777"));
    }

    public LhsLighthouseRpcGatewayResponse.Results createOrganizationResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file("36")
                      .ien(id)
                      .index("1")
                      .status("1")
                      .build()))
          .build();
    }

    private Map<String, LhsLighthouseRpcGatewayResponse.Values> fields() {
      Map<String, LhsLighthouseRpcGatewayResponse.Values> fields = new HashMap<>();
      // Active
      fields.put("#.05", LhsLighthouseRpcGatewayResponse.Values.of(null, null));
      // Address
      fields.put(
          "#.01",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANKS OF FL: EXT", "SHANKS OF FL: IN"));
      fields.put(
          "#.111",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANKSVILLE LINE 1", "SHANKSVILLE LINE 1: IN"));
      fields.put(
          "#.112",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANKSVILLE LINE 2", "SHANKSVILLE LINE 2: IN"));
      fields.put(
          "#.113",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANKSVILLE LINE 3", "SHANKSVILLE LINE 3: IN"));
      fields.put(
          "#.114", LhsLighthouseRpcGatewayResponse.Values.of("SHANK CITY: EXT", "SHANK CITY: IN"));
      fields.put(
          "#.115", LhsLighthouseRpcGatewayResponse.Values.of("SHANKTICUT: EXT", "SHANKTICUT: IN"));
      fields.put(
          "#.116", LhsLighthouseRpcGatewayResponse.Values.of("SHANK ZIP: EXT", "SHANK ZIP: IN"));
      // Contact - Appeals
      fields.put(
          "#.141",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS LINE 1", "SHANK-APPEALS LINE 1: IN"));
      fields.put(
          "#.142",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS LINE 2", "SHANK-APPEALS LINE 2: IN"));
      fields.put(
          "#.143",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS LINE 3", "SHANK-APPEALS LINE 3: IN"));
      fields.put(
          "#.144",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS CITY: EXT", "SHANK-APPEALS CITY: IN"));
      fields.put(
          "#.145", LhsLighthouseRpcGatewayResponse.Values.of("SHANKTICUT: EXT", "SHANKTICUT: IN"));
      fields.put(
          "#.146",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS ZIP: EXT", "SHANK-APPEALS ZIP: IN"));
      fields.put(
          "#.147",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS NAME: EXT", "SHANK-APPEALS NAME: IN"));
      fields.put(
          "#.137",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-APPEALS: EXT", "1-800-SHANK-APPEALS: IN"));
      fields.put(
          "#.149",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "FAX SHANK-APPEALS: EXT", "FAX SHANK-APPEALS: IN"));
      // Contact - Billing
      fields.put(
          "#.117",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-BILLING NAME: EXT", "SHANK-BILLING NAME: IN"));
      fields.put(
          "#.132",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-BILLING: EXT", "1-800-SHANK-BILLING: IN"));
      // Contact - Claims Dental
      fields.put(
          "#.191",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-DENTAL LINE 1: EXT", "SHANK-DENTAL LINE 1: IN"));
      fields.put(
          "#.192",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-DENTAL LINE 2: EXT", "SHANK-DENTAL LINE 2: IN"));
      fields.put(
          "#.194",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-DENTAL CITY: EXT", "SHANK-DENTAL CITY: IN"));
      fields.put(
          "#.195", LhsLighthouseRpcGatewayResponse.Values.of("SHANKTICUT: EXT", "SHANKTICUT: IN"));
      fields.put(
          "#.196",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-DENTAL ZIP: EXT", "SHANK-DENTAL ZIP: IN"));
      fields.put(
          "#.197",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-DENTAL NAME: EXT", "SHANK-DENTAL NAME: IN"));
      fields.put(
          "#.199",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "FAX SHANK-DENTAL: EXT", "FAX SHANK-DENTAL: IN"));
      fields.put(
          "#.1911",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-DENTAL: EXT", "1-800-SHANK-DENTAL: IN"));
      // Contact - Claims Inpt
      fields.put(
          "#.121",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT LINE 1", "SHANK-INPT LINE 1: IN"));
      fields.put(
          "#.122",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT LINE 2", "SHANK-INPT LINE 2: IN"));
      fields.put(
          "#.123",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT LINE 3", "SHANK-INPT LINE 3: IN"));
      fields.put(
          "#.124",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT CITY: EXT", "SHANK-INPT CITY: IN"));
      fields.put(
          "#.125", LhsLighthouseRpcGatewayResponse.Values.of("SHANKTICUT: EXT", "SHANKTICUT: IN"));
      fields.put(
          "#.126",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT ZIP: EXT", "SHANK-INPT ZIP: IN"));
      fields.put(
          "#.127",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT NAME: EXT", "SHANK-INPT NAME: IN"));
      fields.put(
          "#.135",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-INPT: EXT", "1-800-SHANK-INPT: IN"));
      fields.put(
          "#.129",
          LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-INPT: EXT", "FAX SHANK-INPT: IN"));
      // Contact - Opt
      fields.put(
          "#.161",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT LINE 1", "SHANK-OPT LINE 1: IN"));
      fields.put(
          "#.162",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT LINE 2", "SHANK-OPT LINE 2: IN"));
      fields.put(
          "#.163",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT LINE 3", "SHANK-OPT LINE 3: IN"));
      fields.put(
          "#.164",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT CITY: EXT", "SHANK-OPT CITY: IN"));
      fields.put(
          "#.165", LhsLighthouseRpcGatewayResponse.Values.of("SHANKTICUT: EXT", "SHANKTICUT: IN"));
      fields.put(
          "#.166",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT ZIP: EXT", "SHANK-OPT ZIP: IN"));
      fields.put(
          "#.167",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT NAME: EXT", "SHANK-OPT NAME: IN"));
      fields.put(
          "#.136",
          LhsLighthouseRpcGatewayResponse.Values.of("1-800-SHANK-OPT: EXT", "1-800-SHANK-OPT: IN"));
      fields.put(
          "#.169",
          LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-OPT: EXT", "FAX SHANK-OPT: IN"));
      // Contact - RX
      fields.put(
          "#.181",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX LINE 1", "SHANK-RX LINE 1: IN"));
      fields.put(
          "#.182",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX LINE 2", "SHANK-RX LINE 2: IN"));
      fields.put(
          "#.183",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX LINE 3", "SHANK-RX LINE 3: IN"));
      fields.put(
          "#.184",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX CITY: EXT", "SHANK-RX CITY: IN"));
      fields.put(
          "#.185", LhsLighthouseRpcGatewayResponse.Values.of("SHANKTICUT: EXT", "SHANKTICUT: IN"));
      fields.put(
          "#.186",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX ZIP: EXT", "SHANK-RX ZIP: IN"));
      fields.put(
          "#.187",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX NAME: EXT", "SHANK-RX NAME: IN"));
      fields.put(
          "#.1311",
          LhsLighthouseRpcGatewayResponse.Values.of("1-800-SHANK-RX: EXT", "1-800-SHANK-RX: IN"));
      fields.put(
          "#.189",
          LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-RX: EXT", "FAX SHANK-RX: IN"));
      // Contact - Inquiry
      fields.put(
          "#.151",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY LINE 1", "SHANK-INQUIRY LINE 1: IN"));
      fields.put(
          "#.152",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY LINE 2", "SHANK-INQUIRY LINE 2: IN"));
      fields.put(
          "#.153",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY LINE 3", "SHANK-INQUIRY LINE 3: IN"));
      fields.put(
          "#.154",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY CITY: EXT", "SHANK-INQUIRY CITY: IN"));
      fields.put(
          "#.155", LhsLighthouseRpcGatewayResponse.Values.of("SHANKTICUT: EXT", "SHANKTICUT: IN"));
      fields.put(
          "#.156",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY ZIP: EXT", "SHANK-INQUIRY ZIP: IN"));
      fields.put(
          "#.157",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY NAME: EXT", "SHANK-INQUIRY NAME: IN"));
      fields.put(
          "#.138",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-INQUIRY: EXT", "1-800-SHANK-INQUIRY: IN"));
      fields.put(
          "#.159",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "FAX SHANK-INQUIRY: EXT", "FAX SHANK-INQUIRY: IN"));
      // Contact - Precertification
      fields.put(
          "#.133",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-PRECERT: EXT", "1-800-SHANK-PRECERT: IN"));
      fields.put(
          "#.139",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-PRECERT NAME: EXT", "SHANK-PRECERT NAME: IN"));
      // Contact - Verification
      fields.put(
          "#.134",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-VERIFICATION: EXT", "1-800-SHANK-VERIFICATION: IN"));
      // Telecom
      fields.put(
          "#.131", LhsLighthouseRpcGatewayResponse.Values.of("1-800-SHANKTO", "1-800-SHANKTO"));
      fields.put("#.119", LhsLighthouseRpcGatewayResponse.Values.of("SHANKFAX", "SHANKFAX"));
      // Extension
      fields.put("#.06", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.08", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.09", LhsLighthouseRpcGatewayResponse.Values.of("TV/RADIO", "994"));
      fields.put(
          "#.12",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "FILING SHANKTOTIME FRAME: EX", "FILING SHANKTOTIME FRAME: IN"));
      fields.put("#.128", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put(
          "#.13", LhsLighthouseRpcGatewayResponse.Values.of("SHANK INSURANCE", "SHANK INSURANCE"));
      fields.put("#.148", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put(
          "#.15",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK PRESCRIPTION REV CODE: EXT", "SHANK PRESCRIPTION REV CODE: IN"));
      fields.put("#.158", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.168", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.178", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.188", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.198", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.18", LhsLighthouseRpcGatewayResponse.Values.of("DAYS", "DAYS"));
      fields.put("#.19", LhsLighthouseRpcGatewayResponse.Values.of("SHANK FTF VALUE: EXT", "365"));
      fields.put(
          "#1",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK REIMBURSE: EXT", "WILL REIMBURSE"));
      fields.put("#2", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put(
          "#3.01",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANKED ELECTRONICALLY: EXT", "SHANKED ELECTRONICALLY: IN"));
      fields.put(
          "#3.09",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "ELECTRONIC INSHANKANCE TYPE: EXT", "ELECTRONIC INSHANKANCE TYPE: IN"));
      fields.put(
          "#3.1", LhsLighthouseRpcGatewayResponse.Values.of("SHANK PAYER: EXT", "SHANK PAYER: IN"));
      fields.put(
          "#4.01",
          LhsLighthouseRpcGatewayResponse.Values.of("PERF SHANK 1500: EXT", "SHANK 1500: IN"));
      fields.put(
          "#4.02",
          LhsLighthouseRpcGatewayResponse.Values.of("PERF SHANK UB: EXT", "PERF SHANK UB: IN"));
      fields.put(
          "#4.04",
          LhsLighthouseRpcGatewayResponse.Values.of("REF SHANK 1500: EXT", "REF SHANK 1500: IN"));
      fields.put(
          "#4.05",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "REF SHANK CLAIMS: EXT", "REF SHANK CLAIMS: IN"));
      fields.put("#4.06", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#4.08", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#6.09", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#6.1", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      // Identifiers
      fields.put(
          "#3.02", LhsLighthouseRpcGatewayResponse.Values.of("SHANKFEDI: EXT", "SHANKFEDI: IN"));
      fields.put(
          "#3.03", LhsLighthouseRpcGatewayResponse.Values.of("SHANKBIN: EXT", "SHANKBIN: IN"));
      fields.put(
          "#3.04", LhsLighthouseRpcGatewayResponse.Values.of("SHANKTEDI: EXT", "SHANKTEDI: IN"));
      return Map.copyOf(fields);
    }

    LhsLighthouseRpcGatewayResponse.Results getsManifestResults() {
      return getsManifestResults("1,8,");
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file("36")
                      .ien(id)
                      .fields(fields())
                      .build()))
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    private List<Address> address() {
      return List.of(
          Address.builder()
              .line(
                  List.of(
                      "SHANKSVILLE LINE 1: IN", "SHANKSVILLE LINE 2: IN", "SHANKSVILLE LINE 3: IN"))
              .city("SHANK CITY: IN")
              .state("SHANKTICUT: IN")
              .postalCode("SHANK ZIP: IN")
              .build());
    }

    private Organization.Contact appealsContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-APPEALS NAME: IN").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(
                      List.of(
                          "SHANK-APPEALS LINE 1: IN",
                          "SHANK-APPEALS LINE 2: IN",
                          "SHANK-APPEALS LINE 3: IN"))
                  .city("SHANK-APPEALS CITY: IN")
                  .state("SHANKTICUT: IN")
                  .postalCode("SHANK-APPEALS ZIP: IN")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-APPEALS: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-APPEALS: IN")
                      .system(ContactPoint.ContactPointSystem.fax)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("APPEAL")
                              .display("APPEAL")
                              .build()))
                  .build())
          .build();
    }

    private Organization.Contact billingContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-BILLING NAME: IN").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-BILLING: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("BILL")
                              .display("BILL")
                              .build()))
                  .build())
          .build();
    }

    private Organization.Contact claimsDentalContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-DENTAL NAME: IN").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(List.of("SHANK-DENTAL LINE 1: IN", "SHANK-DENTAL LINE 2: IN"))
                  .city("SHANK-DENTAL CITY: IN")
                  .state("SHANKTICUT: IN")
                  .postalCode("SHANK-DENTAL ZIP: IN")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-DENTAL: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-DENTAL: IN")
                      .system(ContactPoint.ContactPointSystem.fax)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("DENTALCLAIM")
                              .display("DENTALCLAIM")
                              .build()))
                  .build())
          .build();
    }

    private Organization.Contact claimsInptContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-INPT NAME: IN").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(
                      List.of(
                          "SHANK-INPT LINE 1: IN",
                          "SHANK-INPT LINE 2: IN",
                          "SHANK-INPT LINE 3: IN"))
                  .city("SHANK-INPT CITY: IN")
                  .state("SHANKTICUT: IN")
                  .postalCode("SHANK-INPT ZIP: IN")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-INPT: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-INPT: IN")
                      .system(ContactPoint.ContactPointSystem.fax)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("INPTCLAIMS")
                              .display("INPTCLAIMS")
                              .build()))
                  .build())
          .build();
    }

    private Organization.Contact claimsOptContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-OPT NAME: IN").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(
                      List.of(
                          "SHANK-OPT LINE 1: IN", "SHANK-OPT LINE 2: IN", "SHANK-OPT LINE 3: IN"))
                  .city("SHANK-OPT CITY: IN")
                  .state("SHANKTICUT: IN")
                  .postalCode("SHANK-OPT ZIP: IN")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-OPT: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-OPT: IN")
                      .system(ContactPoint.ContactPointSystem.fax)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("OUTPTCLAIMS")
                              .display("OUTPTCLAIMS")
                              .build()))
                  .build())
          .build();
    }

    private Organization.Contact claimsRxContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-RX NAME: IN").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(
                      List.of("SHANK-RX LINE 1: IN", "SHANK-RX LINE 2: IN", "SHANK-RX LINE 3: IN"))
                  .city("SHANK-RX CITY: IN")
                  .state("SHANKTICUT: IN")
                  .postalCode("SHANK-RX ZIP: IN")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-RX: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-RX: IN")
                      .system(ContactPoint.ContactPointSystem.fax)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("RXCLAIMS")
                              .display("RXCLAIMS")
                              .build()))
                  .build())
          .build();
    }

    private List<Organization.Contact> contacts() {
      return List.of(
          appealsContact(),
          billingContact(),
          claimsDentalContact(),
          claimsInptContact(),
          claimsOptContact(),
          claimsRxContact(),
          inquiryContact(),
          precertificationContact(),
          verificationContact());
    }

    private List<Extension> extensions(String station) {
      return List.of(
          Extension.builder()
              .valueBoolean(Boolean.TRUE)
              .url("http://va.gov/fhir/StructureDefinition/organization-allowMultipleBedsections")
              .build(),
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/organization-oneOutpatVisitOnBillOnly")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code("994")
                                  .system("urn:oid:2.16.840.1.113883.6.301.3")
                                  .build()))
                      .build())
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-ambulatorySurgeryRevenueCode")
              .build(),
          Extension.builder()
              .valueString("FILING SHANKTOTIME FRAME: IN")
              .url("http://va.gov/fhir/StructureDefinition/organization-filingTimeFrame")
              .build(),
          Extension.builder()
              .valueBoolean(Boolean.TRUE)
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesInpatClaims")
              .build(),
          Extension.builder()
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code("SHANK INSURANCE")
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.36.8013")
                                  .build()))
                      .build())
              .url("http://va.gov/fhir/StructureDefinition/organization-typeOfCoverage")
              .build(),
          Extension.builder()
              .valueBoolean(Boolean.TRUE)
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesAppeals")
              .build(),
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/organization-prescriptionRevenueCode")
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .system("urn:oid:2.16.840.1.113883.6.301.3")
                                  .code("SHANK PRESCRIPTION REV CODE: IN")
                                  .build()))
                      .build())
              .build(),
          Extension.builder()
              .valueBoolean(Boolean.TRUE)
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesInquiries")
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesOutpatClaims")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesPrecert")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesRxClaims")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-anotherCompanyProcessesDentalClaims")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-planStandardFilingTimeFrame")
              .valueQuantity(
                  Quantity.builder()
                      .value(toBigDecimal("365"))
                      .unit("DAYS")
                      .system("urn:oid:2.16.840.1.113883.3.8901.3.3558013")
                      .build())
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-planStandardFilingTimeFrame")
              .build(),
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare")
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code("WILL REIMBURSE")
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.1.36.1")
                                  .build()))
                      .build())
              .build(),
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/organization-signatureRequiredOnBill")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/organization-electronicTransmissionMode")
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.1.36.38001")
                                  .code("SHANKED ELECTRONICALLY: IN")
                                  .build()))
                      .build())
              .build(),
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/organization-electronicInsuranceType")
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code("ELECTRONIC INSHANKANCE TYPE: IN")
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.1.36.38009")
                                  .build()))
                      .build())
              .build(),
          Extension.builder()
              .url(
                  "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
              .valueReference(
                  Reference.builder()
                      .reference(
                          "Organization/"
                              + RecordCoordinates.builder()
                                  .site(station)
                                  .file("365.12")
                                  .ien("SHANK PAYER: IN")
                                  .build()
                                  .toString())
                      .build())
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-performingProviderSecondIDTypeCMS1500")
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001")
                                  .code("SHANK 1500: IN")
                                  .build()))
                      .build())
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-performingProviderSecondIDTypeUB04")
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code("PERF SHANK UB: IN")
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001")
                                  .build()))
                      .build())
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-referrngProviderSecondIDTypeCMS1500")
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code("REF SHANK 1500: IN")
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001")
                                  .build()))
                      .build())
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-referrngProviderSecondIDTypeUB04")
              .valueCodeableConcept(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code("REF SHANK CLAIMS: IN")
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558097.8001")
                                  .build()))
                      .build())
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-attendingRenderingProviderSecondaryIDProfesionalRequired")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-attendingRenderingProviderSecondaryIDInstitutionalRequired")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-printSecTertAutoClaimsLocally")
              .valueBoolean(Boolean.TRUE)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/organization-printSecMedClaimsWOMRALocally")
              .valueBoolean(Boolean.TRUE)
              .build());
    }

    List<Identifier> identifiers() {
      return List.of(
          Identifier.builder()
              .type(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder().id("SHANKFEDI: IN").code("PROFEDI").build()))
                      .build())
              .build(),
          Identifier.builder()
              .type(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder().id("SHANKTEDI: IN").code("INSTEDI").build()))
                      .build())
              .build(),
          Identifier.builder()
              .type(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder().id("SHANKBIN: IN").code("BIN").build()))
                      .build())
              .build());
    }

    private Organization.Contact inquiryContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-INQUIRY NAME: IN").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(
                      List.of(
                          "SHANK-INQUIRY LINE 1: IN",
                          "SHANK-INQUIRY LINE 2: IN",
                          "SHANK-INQUIRY LINE 3: IN"))
                  .city("SHANK-INQUIRY CITY: IN")
                  .state("SHANKTICUT: IN")
                  .postalCode("SHANK-INQUIRY ZIP: IN")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-INQUIRY: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-INQUIRY: IN")
                      .system(ContactPoint.ContactPointSystem.fax)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("INQUIRY")
                              .display("INQUIRY")
                              .build()))
                  .build())
          .build();
    }

    Organization organization() {
      return organization("666", "1,8,");
    }

    Organization organization(String station, String ien) {
      return Organization.builder()
          .id(
              RecordCoordinates.builder()
                  .site(station)
                  .file(InsuranceCompany.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .identifier(identifiers())
          .type(type())
          .name("SHANKS OF FL: IN")
          .address(address())
          .active(Boolean.TRUE)
          .telecom(telecom())
          .contact(contacts())
          .extension(extensions(station))
          .build();
    }

    private Organization.Contact precertificationContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-PRECERT NAME: IN").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-PRECERT: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("PRECERT")
                              .display("PRECERT")
                              .build()))
                  .build())
          .build();
    }

    private List<ContactPoint> telecom() {
      return List.of(
          ContactPoint.builder()
              .value("1-800-SHANKTO")
              .system(ContactPoint.ContactPointSystem.phone)
              .build(),
          ContactPoint.builder()
              .value("SHANKFAX")
              .system(ContactPoint.ContactPointSystem.fax)
              .build());
    }

    private List<CodeableConcept> type() {
      return List.of(
          asCodeableConcept(
              Coding.builder()
                  .code("ins")
                  .display("Insurance Company")
                  .system("http://hl7.org/fhir/ValueSet/organization-type")
                  .build()));
    }

    private Organization.Contact verificationContact() {
      return Organization.Contact.builder()
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-VERIFICATION: IN")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("VERIFY")
                              .display("VERIFY")
                              .build()))
                  .build())
          .build();
    }
  }
}
