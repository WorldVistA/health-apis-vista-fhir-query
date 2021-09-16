package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InsurancePlanSamples {
  @SneakyThrows
  public static String json(Object o) {
    return JacksonConfig.createMapper().writeValueAsString(o);
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    private Map<String, LhsLighthouseRpcGatewayResponse.Values> fields() {
      Map<String, LhsLighthouseRpcGatewayResponse.Values> fields = new HashMap<>();
      fields.put(
          GroupInsurancePlan.INSURANCE_COMPANY,
          LhsLighthouseRpcGatewayResponse.Values.of("BCBS OF SHANKSVILLE", "8"));
      fields.put(
          GroupInsurancePlan.IS_UTILIZATION_REVIEW_REQUIRED,
          LhsLighthouseRpcGatewayResponse.Values.of("YES", "1"));
      fields.put(
          GroupInsurancePlan.IS_PRE_CERTIFICATION_REQUIRED_,
          LhsLighthouseRpcGatewayResponse.Values.of("YES", "1"));
      fields.put(
          GroupInsurancePlan.EXCLUDE_PRE_EXISTING_CONDITION,
          LhsLighthouseRpcGatewayResponse.Values.of("NO", "0"));
      fields.put(
          GroupInsurancePlan.BENEFITS_ASSIGNABLE_,
          LhsLighthouseRpcGatewayResponse.Values.of("YES", "1"));
      fields.put(
          GroupInsurancePlan.TYPE_OF_PLAN,
          LhsLighthouseRpcGatewayResponse.Values.of("PREFERRED PROVIDER ORGANIZATION (PPO)", "40"));
      fields.put(
          GroupInsurancePlan.AMBULATORY_CARE_CERTIFICATION,
          LhsLighthouseRpcGatewayResponse.Values.of("YES", "1"));
      fields.put(
          GroupInsurancePlan.ELECTRONIC_PLAN_TYPE,
          LhsLighthouseRpcGatewayResponse.Values.of("COMMERCIAL", "CI"));
      fields.put(
          GroupInsurancePlan.PLAN_STANDARD_FTF,
          LhsLighthouseRpcGatewayResponse.Values.of("DAYS", "1"));
      fields.put(
          GroupInsurancePlan.PLAN_STANDARD_FTF_VALUE,
          LhsLighthouseRpcGatewayResponse.Values.of("365", "365"));
      fields.put(
          GroupInsurancePlan.GROUP_NAME,
          LhsLighthouseRpcGatewayResponse.Values.of(
              "BCBS OF SHANKSVILLE GROUP", "BCBS OF SHANKSVILLE GROUP"));
      fields.put(
          GroupInsurancePlan.GROUP_NUMBER,
          LhsLighthouseRpcGatewayResponse.Values.of("GRP123456", "GRP123456"));
      fields.put(
          GroupInsurancePlan.PLAN_ID, LhsLighthouseRpcGatewayResponse.Values.of("VA55555", "4444"));
      fields.put(
          GroupInsurancePlan.BANKING_IDENTIFICATION_NUMBER,
          LhsLighthouseRpcGatewayResponse.Values.of("88888888", "88888888"));
      fields.put(
          GroupInsurancePlan.PROCESSOR_CONTROL_NUMBER_PCN_,
          LhsLighthouseRpcGatewayResponse.Values.of("121212121212", "121212121212"));
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
                      .file(GroupInsurancePlan.FILE_NUMBER)
                      .ien(id)
                      .fields(fields())
                      .build()))
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    private List<Extension> extensions() {
      return List.of(
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired")
              .valueBoolean(true)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired")
              .valueBoolean(true)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions")
              .valueBoolean(false)
              .build(),
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable")
              .valueBoolean(true)
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare")
              .valueBoolean(true)
              .build(),
          Extension.builder()
              .valueQuantity(
                  Quantity.builder()
                      .value(new BigDecimal("365"))
                      .unit("DAYS")
                      .system(
                          "http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame")
                      .build())
              .build());
    }

    private List<Identifier> identifiers() {
      return List.of(
          Identifier.builder()
              .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002")
              .value("GRP123456")
              .build(),
          Identifier.builder()
              .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001")
              .value("4444")
              .build(),
          Identifier.builder()
              .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68002")
              .value("88888888")
              .build(),
          Identifier.builder()
              .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68003")
              .value("121212121212")
              .build());
    }

    InsurancePlan insurancePlan() {
      return insurancePlan("666", "1,8,");
    }

    InsurancePlan insurancePlan(String station, String ien) {
      return InsurancePlan.builder()
          .id(
              RecordCoordinates.builder()
                  .site(station)
                  .file(GroupInsurancePlan.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .extension(extensions())
          .identifier(identifiers())
          .name("BCBS OF SHANKSVILLE GROUP")
          .ownedBy(Reference.builder().reference("Organization/" + station + ";36;8").build())
          .plan(plan())
          .type(type())
          .build();
    }

    private List<InsurancePlan.Plan> plan() {
      return List.of(
          InsurancePlan.Plan.builder()
              .type(
                  CodeableConcept.builder()
                      .coding(
                          List.of(
                              Coding.builder()
                                  .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009")
                                  .code("40")
                                  .display("PREFERRED PROVIDER ORGANIZATION (PPO)")
                                  .build()))
                      .text("PREFERRED PROVIDER ORGANIZATION (PPO)")
                      .build())
              .build());
    }

    private List<CodeableConcept> type() {
      return List.of(
          CodeableConcept.builder()
              .coding(
                  List.of(
                      Coding.builder()
                          .system("2.16.840.1.113883.3.8901.3.1.355803.8015")
                          .code("CI")
                          .display("COMMERCIAL")
                          .build()))
              .text("COMMERCIAL")
              .build());
    }
  }
}