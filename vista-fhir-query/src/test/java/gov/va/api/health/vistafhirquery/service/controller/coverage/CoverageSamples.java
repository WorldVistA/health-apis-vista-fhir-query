package gov.va.api.health.vistafhirquery.service.controller.coverage;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.insuranceplan.InsurancePlanStructureDefinitions;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Values;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CoverageSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    public static Coverage.Bundle asBundle(
        String baseUrl, Collection<Coverage> resources, int totalRecords, BundleLink... links) {
      return Coverage.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      resource ->
                          Coverage.Entry.builder()
                              .fullUrl(baseUrl + "/Coverage/" + resource.id())
                              .resource(resource)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static void cleanUpContainedReferencesForComparison(Coverage coverage) {
      // Contained Resource Ids
      coverage.contained().forEach(r -> r.id(null));
      // Insurance Plan Reference
      coverage.coverageClass().forEach(c -> c.value(null));
    }

    public static BundleLink link(BundleLink.LinkRelation rel, String base, String query) {
      return BundleLink.builder().relation(rel).url(base + "?" + query).build();
    }

    private Reference beneficiary(String patient) {
      return Reference.builder()
          .reference("Patient/" + patient)
          .identifier(
              Identifier.builder()
                  .type(
                      CodeableConcept.builder()
                          .coding(
                              List.of(
                                  Coding.builder()
                                      .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                      .code("MB")
                                      .display("Member Number")
                                      .build()))
                          .build())
                  .value("13579")
                  .build())
          .build();
    }

    public Coverage bufferCoverage() {
      return bufferCoverage("666", "1,8,", "1010101010V666666");
    }

    public Coverage bufferCoverage(String station, String ien, String patient) {
      return Coverage.builder()
          .id(
              PatientTypeCoordinates.builder()
                  .icn(patient)
                  .site(station)
                  .file(InsuranceVerificationProcessor.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .type(
              CodeableConcept.builder().coding(Coding.builder().code("1").build().asList()).build())
          .meta(Meta.builder().source(station).build())
          .extension(extensions())
          .status(Status.active)
          .subscriber(Reference.builder().display("Insureds,Name").build())
          .subscriberId("R50797108")
          .beneficiary(beneficiary(patient))
          .dependent("67890")
          .relationship(relationship())
          .period(period())
          .payor(
              List.of(
                  Reference.builder()
                      .reference(
                          "Organization/"
                              + RecordCoordinates.builder()
                                  .site(station)
                                  .file(InsuranceCompany.FILE_NUMBER)
                                  .ien("4")
                                  .build()
                                  .toString())
                      .display("BCBS OF FL")
                      .build()))
          .coverageClass(classes(station, patient))
          .order(1)
          .build();
    }

    private List<Coverage.CoverageClass> classes(String station, String patient) {
      return List.of(
          Coverage.CoverageClass.builder()
              .value(
                  "InsurancePlan/"
                      + RecordCoordinates.builder()
                          .site(station)
                          .file(GroupInsurancePlan.FILE_NUMBER)
                          .ien("87")
                          .build()
                          .toString())
              .type(
                  CodeableConcept.builder()
                      .coding(
                          List.of(
                              Coding.builder()
                                  .system("http://terminology.hl7.org/CodeSystem/coverage-class")
                                  .code("group")
                                  .build()))
                      .build())
              .build());
    }

    private InsurancePlan containedInsurancePlan() {
      return InsurancePlan.builder()
          .name("BCBS OF SHANKSVILLE GROUP")
          .identifier(
              List.of(
                  Identifier.builder()
                      .system(InsurancePlanStructureDefinitions.GROUP_NUMBER)
                      .value("GRP123456")
                      .build()))
          .build();
    }

    public Coverage coverage() {
      return coverage("666", "1,8,", "1010101010V666666");
    }

    public Coverage coverage(String station, String ien, String patient) {
      return Coverage.builder()
          .id(
              PatientTypeCoordinates.builder()
                  .icn(patient)
                  .site(station)
                  .file(InsuranceType.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .meta(Meta.builder().source(station).build())
          .extension(extensions())
          .status(Status.active)
          .subscriberId("R50797108")
          .beneficiary(beneficiary(patient))
          .dependent("67890")
          .relationship(relationship())
          .period(period())
          .payor(
              List.of(
                  Reference.builder()
                      .reference(
                          "Organization/"
                              + RecordCoordinates.builder()
                                  .site(station)
                                  .file(InsuranceCompany.FILE_NUMBER)
                                  .ien("4")
                                  .build()
                                  .toString())
                      .display("BCBS OF FL")
                      .build()))
          .coverageClass(classes(station, patient))
          .order(1)
          .build();
    }

    /* ToDo Remove when insBuffer read and write catch up to eachother: API-13088 */
    public Coverage coverageInsuranceBufferRead(String patient, String station, String ien) {
      return Coverage.builder()
          .id(
              PatientTypeCoordinates.builder()
                  .icn(patient)
                  .site(station)
                  .file(InsuranceVerificationProcessor.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .meta(Meta.builder().source(station).build())
          .contained(List.of(containedInsurancePlan()))
          .status(Status.draft)
          .type(
              CodeableConcept.builder()
                  .coding(
                      Coding.builder()
                          .system(InsuranceBufferStructureDefinitions.INQ_SERVICE_TYPE_CODE)
                          .code("1")
                          .build()
                          .asList())
                  .build())
          .subscriberId("R50797108")
          .beneficiary(beneficiary(patient))
          .relationship(relationship())
          .coverageClass(classes(station, patient).get(0).value(null).asList())
          .build();
    }

    private List<Extension> extensions() {
      return List.of(
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/coverage-stopPolicyFromBilling")
              .valueBoolean(true)
              .build());
    }

    private Period period() {
      return Period.builder().start("1992-01-12T00:00:00Z").end("2025-01-01T00:00:00Z").build();
    }

    private CodeableConcept relationship() {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .system("http://terminology.hl7.org/CodeSystem/subscriber-relationship")
                      .code("spouse")
                      .display("Spouse")
                      .build()))
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    public Set<WriteableFilemanValue> createApiInput() {
      return Set.of(
          pointerTo(InsuranceCompany.FILE_NUMBER, "4"),
          pointerTo(GroupInsurancePlan.FILE_NUMBER, "87"),
          insuranceValue(InsuranceType.COORDINATION_OF_BENEFITS, "PRIMARY"),
          insuranceValue(InsuranceType.INSURANCE_EXPIRATION_DATE, "01-01-2025"),
          insuranceValue(InsuranceType.PT_RELATIONSHIP_HIPAA, "SPOUSE"),
          insuranceValue(InsuranceType.PHARMACY_PERSON_CODE, "67890"),
          insuranceValue(InsuranceType.PATIENT_ID, "13579"),
          insuranceValue(InsuranceType.SUBSCRIBER_ID, "R50797108"),
          insuranceValue(InsuranceType.EFFECTIVE_DATE_OF_POLICY, "01-12-1992"),
          insuranceValue(InsuranceType.STOP_POLICY_FROM_BILLING, "YES"));
    }

    public Set<WriteableFilemanValue> createInsuranceBufferInput() {
      return Set.of(
          pointerTo("355.12", "22"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME,
              "Placeholder Insurance Company Name"),
          insuranceBufferValue(InsuranceVerificationProcessor.WHOSE_INSURANCE, "s"),
          insuranceBufferValue(InsuranceVerificationProcessor.NAME_OF_INSURED, "Insureds,Name"),
          insuranceBufferValue(InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA, "SPOUSE"),
          insuranceBufferValue(InsuranceVerificationProcessor.PATIENT_ID, "13579"),
          insuranceBufferValue(InsuranceVerificationProcessor.OVERRIDE_FRESHNESS_FLAG, "0"),
          insuranceBufferValue(InsuranceVerificationProcessor.STATUS, "E"),
          insuranceBufferValue(InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1, "1"),
          insuranceBufferValue(InsuranceVerificationProcessor.SUBSCRIBER_ID, "R50797108"));
    }

    public LhsLighthouseRpcGatewayResponse.Results createInsuranceBufferResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file(InsuranceVerificationProcessor.FILE_NUMBER)
                      .ien(id)
                      .index("1")
                      .status("1")
                      .fields(insuranceBufferFields())
                      .build()))
          .build();
    }

    public LhsLighthouseRpcGatewayResponse.Results createInsuranceTypeResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file(InsuranceType.FILE_NUMBER)
                      .ien(id)
                      .index("1")
                      .status("1")
                      .build()))
          .build();
    }

    private Map<String, Values> fields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(InsuranceType.INSURANCE_TYPE, Values.of("BCBS OF FL", "4"));
      fields.put(InsuranceType.GROUP_PLAN, Values.of("BCBS OF FL", "87"));
      fields.put(InsuranceType.COORDINATION_OF_BENEFITS, Values.of("PRIMARY", "1"));
      fields.put(InsuranceType.SEND_BILL_TO_EMPLOYER, Values.of("0", "0"));
      fields.put(InsuranceType.ESGHP, Values.of("3", "3"));
      fields.put(InsuranceType.INSURANCE_EXPIRATION_DATE, Values.of("JAN 01, 2025", "3250101"));
      fields.put(InsuranceType.STOP_POLICY_FROM_BILLING, Values.of("1", "1"));
      fields.put(InsuranceType.PT_RELATIONSHIP_HIPAA, Values.of("SPOUSE", "01"));
      fields.put(InsuranceType.PHARMACY_PERSON_CODE, Values.of("67890", "67890"));
      fields.put(InsuranceType.PATIENT_ID, Values.of("13579", "13579"));
      fields.put(InsuranceType.SUBSCRIBER_ID, Values.of("R50797108", "R50797108"));
      fields.put(InsuranceType.EFFECTIVE_DATE_OF_POLICY, Values.of("JAN 12, 1992", "2920112"));
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
                      .file("2.312")
                      .ien(id)
                      .fields(fields())
                      .build()))
          .build();
    }

    private Map<String, Values> insuranceBufferFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1, Values.of("1", "1"));
      fields.put(InsuranceVerificationProcessor.PATIENT_ID, Values.of("13579", "13579"));
      fields.put(InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA, Values.of("SPOUSE", "01"));
      fields.put(InsuranceVerificationProcessor.SUBSCRIBER_ID, Values.of("R50797108", "R50797108"));
      fields.put(
          InsuranceVerificationProcessor.GROUP_NAME,
          Values.of("BCBS OF SHANKSVILLE GROUP", "BCBS OF SHANKSVILLE GROUP"));
      fields.put(InsuranceVerificationProcessor.GROUP_NUMBER, Values.of("GRP123456", "GRP123456"));
      return Map.copyOf(fields);
    }

    private WriteableFilemanValue insuranceBufferValue(String field, String value) {
      return WriteableFilemanValue.builder()
          .file(InsuranceVerificationProcessor.FILE_NUMBER)
          .index(1)
          .field(field)
          .value(value)
          .build();
    }

    private WriteableFilemanValue insuranceValue(String field, String value) {
      return WriteableFilemanValue.builder()
          .file(InsuranceType.FILE_NUMBER)
          .index(1)
          .field(field)
          .value(value)
          .build();
    }

    private WriteableFilemanValue pointerTo(String file, String ien) {
      return WriteableFilemanValue.builder().file(file).index(1).field("ien").value(ien).build();
    }

    public Set<WriteableFilemanValue> updateApiInput() {
      var filemanValues = new HashSet<>(createApiInput());
      filemanValues.add(pointerTo(InsuranceType.FILE_NUMBER, "1,8,"));
      return Set.copyOf(filemanValues);
    }

    public LhsLighthouseRpcGatewayResponse.Results updateCoverageWithNotExistsId() {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file("2.312")
                      .ien("doesnt-exist")
                      .index("1")
                      .status("-1")
                      .build()))
          .errors(
              List.of(
                  LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                      .data(
                          Map.of(
                              "code",
                              "601",
                              "location",
                              "FILE^LHSIBUTL",
                              "text",
                              "The entry does not exist."))
                      .build()))
          .build();
    }
  }
}
