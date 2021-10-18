package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toBigDecimal;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrganizationSamples {
  @SneakyThrows
  public static String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public static LinkProperties linkProperties() {
    return LinkProperties.builder()
        .publicUrl("http://fake.com")
        .publicR4BasePath("hcs/{site}/r4")
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
      return createApiInput("1,8");
    }

    public Set<WriteableFilemanValue> createApiInput(String ien) {
      return Set.of(
          WriteableFilemanValue.builder()
              .file(InsuranceCompany.FILE_NUMBER)
              .field("ien")
              .index(1)
              .value(ien)
              .build(),
          insuranceCompanyValue(InsuranceCompany.NAME, "SHANKS OF FL"),
          insuranceCompanyValue(InsuranceCompany.CITY, "SHANK CITY"),
          insuranceCompanyValue(InsuranceCompany.STATE, "FLORIDA"),
          insuranceCompanyValue(InsuranceCompany.STREET_ADDRESS_LINE_1_, "SHANKSVILLE LINE 1"),
          insuranceCompanyValue(InsuranceCompany.STREET_ADDRESS_LINE_2_, "SHANKSVILLE LINE 2"),
          insuranceCompanyValue(InsuranceCompany.STREET_ADDRESS_LINE_3_, "SHANKSVILLE LINE 3"),
          insuranceCompanyValue(InsuranceCompany.ZIP_CODE, "322310014"),
          insuranceCompanyValue(InsuranceCompany.BILLING_COMPANY_NAME, "SHANK-BILLING"),
          insuranceCompanyValue(InsuranceCompany.FAX_NUMBER, "SHANKFAX"),
          insuranceCompanyValue(InsuranceCompany.TYPE_OF_COVERAGE, "HEALTH INSURANCE"),
          insuranceCompanyValue(InsuranceCompany.PHONE_NUMBER, "800-456-8888"),
          insuranceCompanyValue(InsuranceCompany.BILLING_PHONE_NUMBER, "800-123-7777"),
          insuranceCompanyValue(InsuranceCompany.PRECERTIFICATION_PHONE_NUMBER, "800-222-9999"),
          insuranceCompanyValue(InsuranceCompany.VERIFICATION_PHONE_NUMBER, "800-333-8888"),
          insuranceCompanyValue(InsuranceCompany.CLAIMS_INPT_PHONE_NUMBER, "800-444-7777"),
          insuranceCompanyValue(InsuranceCompany.CLAIMS_OPT_PHONE_NUMBER, "800-555-6666"),
          insuranceCompanyValue(InsuranceCompany.APPEALS_PHONE_NUMBER, "1-800-SHANK-APPEALS"),
          insuranceCompanyValue(InsuranceCompany.INQUIRY_PHONE_NUMBER, "1-800-SHANK-INQUIRY"),
          insuranceCompanyValue(InsuranceCompany.REIMBURSE_, "WILL REIMBURSE"),
          insuranceCompanyValue(InsuranceCompany.SIGNATURE_REQUIRED_ON_BILL_, "YES"),
          insuranceCompanyValue(InsuranceCompany.TRANSMIT_ELECTRONICALLY, "YES-TEST"),
          insuranceCompanyValue(InsuranceCompany.ELECTRONIC_INSURANCE_TYPE, "GROUP POLICY"),
          insuranceCompanyValue(InsuranceCompany.REF_PROV_SEC_ID_REQ_ON_CLAIMS, "NONE"),
          insuranceCompanyValue(InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_PROF, "YES"),
          insuranceCompanyValue(InsuranceCompany.ATT_REND_ID_BILL_SEC_ID_INST, "YES"),
          insuranceCompanyValue(InsuranceCompany.PRINT_SEC_TERT_AUTO_CLAIMS_, "YES"),
          insuranceCompanyValue(InsuranceCompany.PRINT_SEC_MED_CLAIMS_W_O_MRA_, "YES"),
          insuranceCompanyValue(InsuranceCompany.BIN_NUMBER, "SHANKBIN"),
          insuranceCompanyValue(InsuranceCompany.EDI_ID_NUMBER_INST, "66666"),
          insuranceCompanyValue(InsuranceCompany.EDI_ID_NUMBER_PROF, "55555"),
          insuranceCompanyValue(InsuranceCompany.REF_PROV_SEC_ID_DEF_CMS_1500, "REF PROV CMS 1500"),
          insuranceCompanyValue(
              InsuranceCompany.PRESCRIPTION_REFILL_REV_CODE, "SHANK PRESCRIPTION REV CODE"),
          insuranceCompanyValue(InsuranceCompany.ANOTHER_CO_PROCESS_INQUIRIES_, "YES"),
          insuranceCompanyValue(InsuranceCompany.AMBULATORY_SURG_REV_CODE, "994"),
          insuranceCompanyValue(InsuranceCompany.ONE_OPT_VISIT_ON_BILL_ONLY, "YES"),
          insuranceCompanyValue(InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_1500, "PERF PROV 1500"),
          insuranceCompanyValue(InsuranceCompany.ANOTHER_CO_PROCESS_RX_CLAIMS_, "YES"),
          pointerTo("365.12", "SHANK PAYER"),
          insuranceCompanyValue(InsuranceCompany.ALLOW_MULTIPLE_BEDSECTIONS, "YES"),
          insuranceCompanyValue(InsuranceCompany.ANOTHER_CO_PROCESS_OP_CLAIMS_, "YES"),
          insuranceCompanyValue(InsuranceCompany.ANOTHER_CO_PROCESS_APPEALS_, "YES"),
          insuranceCompanyValue(InsuranceCompany.PERF_PROV_SECOND_ID_TYPE_UB, "PERF PROV 04"),
          insuranceCompanyValue(InsuranceCompany.ANOTHER_CO_PROC_DENT_CLAIMS_, "YES"),
          insuranceCompanyValue(InsuranceCompany.ANOTHER_CO_PROCESS_IP_CLAIMS_, "YES"),
          insuranceCompanyValue(InsuranceCompany.ANOTHER_CO_PROCESS_PRECERTS_, "YES"),
          insuranceCompanyValue(InsuranceCompany.STANDARD_FTF, "DAYS"),
          insuranceCompanyValue(InsuranceCompany.STANDARD_FTF_VALUE, "365"),
          insuranceCompanyValue(InsuranceCompany.FILING_TIME_FRAME, "FILING SHANKTOTIME FRAME"));
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
      fields.put("#.01", LhsLighthouseRpcGatewayResponse.Values.of("SHANKS OF FL", "SHANKS OF FL"));
      fields.put(
          "#.111",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANKSVILLE LINE 1", "SHANKSVILLE LINE 1"));
      fields.put(
          "#.112",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANKSVILLE LINE 2", "SHANKSVILLE LINE 2"));
      fields.put(
          "#.113",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANKSVILLE LINE 3", "SHANKSVILLE LINE 3"));
      fields.put("#.114", LhsLighthouseRpcGatewayResponse.Values.of("SHANK CITY", "SHANK CITY"));
      fields.put("#.115", LhsLighthouseRpcGatewayResponse.Values.of("FLORIDA", "12"));
      fields.put("#.116", LhsLighthouseRpcGatewayResponse.Values.of("322310014", "322310014"));
      // Contact - Appeals
      fields.put(
          "#.141",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS LINE 1", "SHANK-APPEALS LINE 1"));
      fields.put(
          "#.142",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS LINE 2", "SHANK-APPEALS LINE 2"));
      fields.put(
          "#.143",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS LINE 3", "SHANK-APPEALS LINE 3"));
      fields.put(
          "#.144",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS CITY: EXT", "SHANK-APPEALS CITY"));
      fields.put("#.145", LhsLighthouseRpcGatewayResponse.Values.of("FLORIDA", "12"));
      fields.put(
          "#.146",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-APPEALS ZIP: EXT", "SHANK-APPEALS ZIP"));
      fields.put(
          "#.147",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-APPEALS NAME: EXT", "SHANK-APPEALS NAME"));
      fields.put(
          "#.137",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-APPEALS: EXT", "1-800-SHANK-APPEALS"));
      fields.put(
          "#.149",
          LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-APPEALS: EXT", "FAX SHANK-APPEALS"));
      // Contact - Billing
      fields.put(
          "#.117", LhsLighthouseRpcGatewayResponse.Values.of("SHANK-BILLING", "SHANK-BILLING"));
      fields.put(
          "#.132",
          LhsLighthouseRpcGatewayResponse.Values.of("1-800-SHANK-BILLING: EXT", "800-123-7777"));
      // Contact - Claims Dental
      fields.put(
          "#.191",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-DENTAL LINE 1: EXT", "SHANK-DENTAL LINE 1"));
      fields.put(
          "#.192",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-DENTAL LINE 2: EXT", "SHANK-DENTAL LINE 2"));
      fields.put(
          "#.194",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-DENTAL CITY: EXT", "SHANK-DENTAL CITY"));
      fields.put("#.195", LhsLighthouseRpcGatewayResponse.Values.of("FLORIDA", "12"));
      fields.put(
          "#.196",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-DENTAL ZIP: EXT", "SHANK-DENTAL ZIP"));
      fields.put(
          "#.197",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-DENTAL NAME: EXT", "SHANK-DENTAL NAME"));
      fields.put(
          "#.199",
          LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-DENTAL: EXT", "FAX SHANK-DENTAL"));
      fields.put(
          "#.1911",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-DENTAL: EXT", "1-800-SHANK-DENTAL"));
      // Contact - Claims Inpt
      fields.put(
          "#.121",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT LINE 1", "SHANK-INPT LINE 1"));
      fields.put(
          "#.122",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT LINE 2", "SHANK-INPT LINE 2"));
      fields.put(
          "#.123",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT LINE 3", "SHANK-INPT LINE 3"));
      fields.put(
          "#.124",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT CITY: EXT", "SHANK-INPT CITY"));
      fields.put("#.125", LhsLighthouseRpcGatewayResponse.Values.of("FLORIDA", "12"));
      fields.put(
          "#.126",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT ZIP: EXT", "SHANK-INPT ZIP"));
      fields.put(
          "#.127",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INPT NAME: EXT", "SHANK-INPT NAME"));
      fields.put(
          "#.135", LhsLighthouseRpcGatewayResponse.Values.of("800-444-7777", "800-444-7777"));
      fields.put(
          "#.129",
          LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-INPT: EXT", "FAX SHANK-INPT"));
      // Contact - Opt
      fields.put(
          "#.161",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT LINE 1", "SHANK-OPT LINE 1"));
      fields.put(
          "#.162",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT LINE 2", "SHANK-OPT LINE 2"));
      fields.put(
          "#.163",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT LINE 3", "SHANK-OPT LINE 3"));
      fields.put(
          "#.164",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT CITY: EXT", "SHANK-OPT CITY"));
      fields.put("#.165", LhsLighthouseRpcGatewayResponse.Values.of("FLORIDA", "12"));
      fields.put(
          "#.166",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT ZIP: EXT", "SHANK-OPT ZIP"));
      fields.put(
          "#.167",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-OPT NAME: EXT", "SHANK-OPT NAME"));
      fields.put(
          "#.136", LhsLighthouseRpcGatewayResponse.Values.of("800-555-6666", "800-555-6666"));
      fields.put(
          "#.169",
          LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-OPT: EXT", "FAX SHANK-OPT"));
      // Contact - RX
      fields.put(
          "#.181", LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX LINE 1", "SHANK-RX LINE 1"));
      fields.put(
          "#.182", LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX LINE 2", "SHANK-RX LINE 2"));
      fields.put(
          "#.183", LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX LINE 3", "SHANK-RX LINE 3"));
      fields.put(
          "#.184",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX CITY: EXT", "SHANK-RX CITY"));
      fields.put("#.185", LhsLighthouseRpcGatewayResponse.Values.of("FLORIDA", "12"));
      fields.put(
          "#.186", LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX ZIP: EXT", "SHANK-RX ZIP"));
      fields.put(
          "#.187",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-RX NAME: EXT", "SHANK-RX NAME"));
      fields.put(
          "#.1311",
          LhsLighthouseRpcGatewayResponse.Values.of("1-800-SHANK-RX: EXT", "1-800-SHANK-RX"));
      fields.put(
          "#.189", LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-RX: EXT", "FAX SHANK-RX"));
      // Contact - Inquiry
      fields.put(
          "#.151",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY LINE 1", "SHANK-INQUIRY LINE 1"));
      fields.put(
          "#.152",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY LINE 2", "SHANK-INQUIRY LINE 2"));
      fields.put(
          "#.153",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY LINE 3", "SHANK-INQUIRY LINE 3"));
      fields.put(
          "#.154",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY CITY: EXT", "SHANK-INQUIRY CITY"));
      fields.put("#.155", LhsLighthouseRpcGatewayResponse.Values.of("FLORIDA", "12"));
      fields.put(
          "#.156",
          LhsLighthouseRpcGatewayResponse.Values.of("SHANK-INQUIRY ZIP: EXT", "SHANK-INQUIRY ZIP"));
      fields.put(
          "#.157",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-INQUIRY NAME: EXT", "SHANK-INQUIRY NAME"));
      fields.put(
          "#.138",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "1-800-SHANK-INQUIRY: EXT", "1-800-SHANK-INQUIRY"));
      fields.put(
          "#.159",
          LhsLighthouseRpcGatewayResponse.Values.of("FAX SHANK-INQUIRY: EXT", "FAX SHANK-INQUIRY"));
      // Contact - Precertification
      fields.put(
          "#.133", LhsLighthouseRpcGatewayResponse.Values.of("800-222-9999", "800-222-9999"));
      fields.put(
          "#.139",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK-PRECERT NAME: EXT", "SHANK-PRECERT NAME"));
      // Contact - Verification
      fields.put(
          "#.134", LhsLighthouseRpcGatewayResponse.Values.of("800-333-8888", "800-333-8888"));
      // Telecom
      fields.put(
          "#.131", LhsLighthouseRpcGatewayResponse.Values.of("800-456-8888", "800-456-8888"));
      fields.put("#.119", LhsLighthouseRpcGatewayResponse.Values.of("SHANKFAX", "SHANKFAX"));
      // Extension
      fields.put("#.06", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.08", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.09", LhsLighthouseRpcGatewayResponse.Values.of("TV/RADIO", "994"));
      fields.put(
          "#.12",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "FILING SHANKTOTIME FRAME: EX", "FILING SHANKTOTIME FRAME"));
      fields.put("#.128", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.13", LhsLighthouseRpcGatewayResponse.Values.of("HEALTH INSURANCE", "1"));
      fields.put("#.148", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put(
          "#.15",
          LhsLighthouseRpcGatewayResponse.Values.of(
              "SHANK PRESCRIPTION REV CODE: EXT", "SHANK PRESCRIPTION REV CODE"));
      fields.put("#.158", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.168", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.178", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.188", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.198", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#.18", LhsLighthouseRpcGatewayResponse.Values.of("DAYS", "DAYS"));
      fields.put("#.19", LhsLighthouseRpcGatewayResponse.Values.of("365", "365"));
      fields.put("#1", LhsLighthouseRpcGatewayResponse.Values.of("WILL REIMBURSE", "Y"));
      fields.put("#2", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#3.01", LhsLighthouseRpcGatewayResponse.Values.of("YES-TEST", "2"));
      fields.put("#3.09", LhsLighthouseRpcGatewayResponse.Values.of("GROUP POLICY", "5"));
      fields.put(
          "#3.1", LhsLighthouseRpcGatewayResponse.Values.of("SHANK PAYER: EXT", "SHANK PAYER"));
      fields.put(
          "#4.01", LhsLighthouseRpcGatewayResponse.Values.of("PERF PROF 1500", "PERF PROV 1500"));
      fields.put(
          "#4.02", LhsLighthouseRpcGatewayResponse.Values.of("PERF PROV 04", "PERF PROV 04"));
      fields.put(
          "#4.04",
          LhsLighthouseRpcGatewayResponse.Values.of("REF PROV CMS 1500", "REF PROV CMS 1500"));
      fields.put("#4.05", LhsLighthouseRpcGatewayResponse.Values.of("NONE", "0"));
      fields.put("#4.06", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#4.08", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#6.09", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      fields.put("#6.1", LhsLighthouseRpcGatewayResponse.Values.of("TRUE", "1"));
      // Identifiers
      fields.put("#3.02", LhsLighthouseRpcGatewayResponse.Values.of("55555", "55555"));
      fields.put("#3.03", LhsLighthouseRpcGatewayResponse.Values.of("SHANKBIN: EXT", "SHANKBIN"));
      fields.put("#3.04", LhsLighthouseRpcGatewayResponse.Values.of("66666", "66666"));
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
    public static Organization.Bundle asBundle(
        String baseUrl, Collection<Organization> resources, int totalRecords, BundleLink... links) {
      return Organization.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      resource ->
                          Organization.Entry.builder()
                              .fullUrl(baseUrl + "/Organization/" + resource.id())
                              .resource(resource)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static BundleLink link(BundleLink.LinkRelation rel, String base, String query) {
      return BundleLink.builder().relation(rel).url(base + "?" + query).build();
    }

    public static Organization sortExtensions(Organization org) {
      var sorted = new ArrayList<>(org.extension());
      sorted.sort((e1, e2) -> String.CASE_INSENSITIVE_ORDER.compare(e1.url(), e2.url()));
      org.extension(sorted);
      return org;
    }

    public static Organization.Bundle sortExtensions(Organization.Bundle bundle) {
      bundle.entry().forEach(entry -> sortExtensions(entry.resource()));
      return bundle;
    }

    private List<Address> address() {
      return List.of(
          Address.builder()
              .line(List.of("SHANKSVILLE LINE 1", "SHANKSVILLE LINE 2", "SHANKSVILLE LINE 3"))
              .city("SHANK CITY")
              .state("FLORIDA")
              .postalCode("322310014")
              .build());
    }

    private Organization.Contact appealsContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-APPEALS NAME").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(
                      List.of(
                          "SHANK-APPEALS LINE 1", "SHANK-APPEALS LINE 2", "SHANK-APPEALS LINE 3"))
                  .city("SHANK-APPEALS CITY")
                  .state("FLORIDA")
                  .postalCode("SHANK-APPEALS ZIP")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-APPEALS")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-APPEALS")
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
                      .valueReference(Reference.builder().display("SHANK-BILLING").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("800-123-7777")
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
                      .valueReference(Reference.builder().display("SHANK-DENTAL NAME").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(List.of("SHANK-DENTAL LINE 1", "SHANK-DENTAL LINE 2"))
                  .city("SHANK-DENTAL CITY")
                  .state("FLORIDA")
                  .postalCode("SHANK-DENTAL ZIP")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-DENTAL")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-DENTAL")
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
                      .valueReference(Reference.builder().display("SHANK-INPT NAME").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(List.of("SHANK-INPT LINE 1", "SHANK-INPT LINE 2", "SHANK-INPT LINE 3"))
                  .city("SHANK-INPT CITY")
                  .state("FLORIDA")
                  .postalCode("SHANK-INPT ZIP")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("800-444-7777")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-INPT")
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
                      .valueReference(Reference.builder().display("SHANK-OPT NAME").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(List.of("SHANK-OPT LINE 1", "SHANK-OPT LINE 2", "SHANK-OPT LINE 3"))
                  .city("SHANK-OPT CITY")
                  .state("FLORIDA")
                  .postalCode("SHANK-OPT ZIP")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("800-555-6666")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-OPT")
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
                      .valueReference(Reference.builder().display("SHANK-RX NAME").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(List.of("SHANK-RX LINE 1", "SHANK-RX LINE 2", "SHANK-RX LINE 3"))
                  .city("SHANK-RX CITY")
                  .state("FLORIDA")
                  .postalCode("SHANK-RX ZIP")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-RX")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-RX")
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
      return Stream.of(
              Extension.builder()
                  .valueBoolean(Boolean.TRUE)
                  .url(OrganizationStructureDefinitions.ALLOW_MULTIPLE_BEDSECTIONS)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.ONE_OUTPAT_VISIT_ON_BILL_ONLY)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              Extension.builder()
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .code("994")
                                      .system(
                                          OrganizationStructureDefinitions
                                              .AMBULATORY_SURGERY_REVENUE_CODE_URN_OID)
                                      .build()))
                          .build())
                  .url(OrganizationStructureDefinitions.AMBULATORY_SURGERY_REVENUE_CODE)
                  .build(),
              Extension.builder()
                  .valueBoolean(Boolean.TRUE)
                  .url(OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_INPAT_CLAIMS)
                  .build(),
              typeOfCoverage(),
              Extension.builder()
                  .valueBoolean(Boolean.TRUE)
                  .url(OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_APPEALS)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.PRESCRIPTION_REVENUE_CODE)
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .system(
                                          OrganizationStructureDefinitions
                                              .PRESCRIPTION_REVENUE_CODE_URN_OID)
                                      .code("SHANK PRESCRIPTION REV CODE")
                                      .build()))
                          .build())
                  .build(),
              Extension.builder()
                  .valueBoolean(Boolean.TRUE)
                  .url(OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_INQUIRIES)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_OUTPAT_CLAIMS)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_PRECERT)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_RX_CLAIMS)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.ANOTHER_COMPANY_PROCESSES_DENTAL_CLAIMS)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.WILL_REIMBURSE_FOR_CARE)
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .code("WILL REIMBURSE")
                                      .system(
                                          OrganizationStructureDefinitions
                                              .WILL_REIMBURSE_FOR_CARE_URN_OID)
                                      .build()))
                          .build())
                  .build(),
              signatureRequiredOnBill(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.ELECTRONIC_TRANSMISSION_MODE)
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .system(
                                          OrganizationStructureDefinitions
                                              .ELECTRONIC_TRANSMISSION_MODE_URN_OID)
                                      .code("YES-TEST")
                                      .build()))
                          .build())
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.ELECTRONIC_INSURANCE_TYPE)
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .code("GROUP POLICY")
                                      .system(
                                          OrganizationStructureDefinitions
                                              .ELECTRONIC_INSURANCE_TYPE_URN_OID)
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
                                      .ien("SHANK PAYER")
                                      .build()
                                      .toString())
                          .build())
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500)
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .system(
                                          OrganizationStructureDefinitions
                                              .PERFORMING_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID)
                                      .code("PERF PROV 1500")
                                      .build()))
                          .build())
                  .build(),
              Extension.builder()
                  .url("http://va.gov/fhir/StructureDefinition/organization-filingTimeFrame")
                  .valueString("FILING SHANKTOTIME FRAME")
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04)
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .system(
                                          OrganizationStructureDefinitions
                                              .PERFORMING_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID)
                                      .code("PERF PROV 04")
                                      .build()))
                          .build())
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.REFERRNG_PROVIDER_SECOND_IDTYPE_CMS_1500)
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .code("REF PROV CMS 1500")
                                      .system(
                                          OrganizationStructureDefinitions
                                              .REFERRNG_PROVIDER_SECOND_IDTYPE_CMS_1500_URN_OID)
                                      .build()))
                          .build())
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.REFERRNG_PROVIDER_SECOND_IDTYPE_UB_04)
                  .valueCodeableConcept(
                      CodeableConcept.builder()
                          .coding(
                              Collections.singletonList(
                                  Coding.builder()
                                      .code("NONE")
                                      .system(
                                          OrganizationStructureDefinitions
                                              .REFERRNG_PROVIDER_SECOND_IDTYPE_UB_04_URN_OID)
                                      .build()))
                          .build())
                  .build(),
              Extension.builder()
                  .url(
                      OrganizationStructureDefinitions
                          .ATTENDING_RENDERING_PROVIDER_SECONDARY_IDPROFESIONAL_REQUIRED)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              Extension.builder()
                  .url(
                      OrganizationStructureDefinitions
                          .ATTENDING_RENDERING_PROVIDER_SECONDARY_IDINSTITUTIONAL_REQUIRED)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.PRINT_SEC_TERT_AUTO_CLAIMS_LOCALLY)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              Extension.builder()
                  .url(OrganizationStructureDefinitions.PRINT_SEC_MED_CLAIMS_WOMRALOCALLY)
                  .valueBoolean(Boolean.TRUE)
                  .build(),
              ftfExtension())
          .toList();
    }

    Extension ftfExtension() {
      return Extension.builder()
          .valueQuantity(
              Quantity.builder()
                  .value(toBigDecimal("365"))
                  .unit("DAYS")
                  .system(OrganizationStructureDefinitions.PLAN_STANDARD_FILING_TIME_FRAME_URN_OID)
                  .build())
          .url(OrganizationStructureDefinitions.PLAN_STANDARD_FILING_TIME_FRAME)
          .build();
    }

    List<Identifier> identifiers() {
      return List.of(
          Identifier.builder()
              .value("55555")
              .type(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code(OrganizationStructureDefinitions.EDI_ID_NUMBER_PROF_CODE)
                                  .build()))
                      .build())
              .build(),
          Identifier.builder()
              .value("66666")
              .type(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code(OrganizationStructureDefinitions.EDI_ID_NUMBER_INST_CODE)
                                  .build()))
                      .build())
              .build(),
          Identifier.builder()
              .value("SHANKBIN")
              .type(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .code(OrganizationStructureDefinitions.BIN_NUMBER_CODE)
                                  .build()))
                      .build())
              .build());
    }

    private Organization.Contact inquiryContact() {
      return Organization.Contact.builder()
          .extension(
              List.of(
                  Extension.builder()
                      .valueReference(Reference.builder().display("SHANK-INQUIRY NAME").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .address(
              Address.builder()
                  .line(
                      List.of(
                          "SHANK-INQUIRY LINE 1", "SHANK-INQUIRY LINE 2", "SHANK-INQUIRY LINE 3"))
                  .city("SHANK-INQUIRY CITY")
                  .state("FLORIDA")
                  .postalCode("SHANK-INQUIRY ZIP")
                  .build())
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("1-800-SHANK-INQUIRY")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build(),
                  ContactPoint.builder()
                      .value("FAX SHANK-INQUIRY")
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
          .meta(Meta.builder().source(station).build())
          .id(
              RecordCoordinates.builder()
                  .site(station)
                  .file(InsuranceCompany.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .identifier(identifiers())
          .type(type())
          .name("SHANKS OF FL")
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
                      .valueReference(Reference.builder().display("SHANK-PRECERT NAME").build())
                      .url(
                          "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                      .build()))
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("800-222-9999")
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

    public Extension signatureRequiredOnBill() {
      return Extension.builder()
          .url(OrganizationStructureDefinitions.SIGNATURE_REQUIRED_ON_BILL)
          .valueBoolean(true)
          .build();
    }

    private List<ContactPoint> telecom() {
      return List.of(
          ContactPoint.builder()
              .value("800-456-8888")
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

    public Extension typeOfCoverage() {
      return Extension.builder()
          .valueCodeableConcept(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .code("HEALTH INSURANCE")
                              .system(OrganizationStructureDefinitions.TYPE_OF_COVERAGE_URN_OID)
                              .build()))
                  .build())
          .url(OrganizationStructureDefinitions.TYPE_OF_COVERAGE)
          .build();
    }

    private Organization.Contact verificationContact() {
      return Organization.Contact.builder()
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("800-333-8888")
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
