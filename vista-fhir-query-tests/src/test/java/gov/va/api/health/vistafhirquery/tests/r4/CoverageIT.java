package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class CoverageIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Delegate
  private final ResourceVerifier verifier = VistaFhirQueryResourceVerifier.r4ForSite("673");

  @Test
  @Order(1)
  @SneakyThrows
  void create() {
    assumeEnvironmentNotIn(Environment.LOCAL, Environment.STAGING, Environment.PROD);
    var postBody = JacksonConfig.createMapper().writeValueAsString(insuranceBufferCreate());
    verify(
        test(
            201,
            Void.class,
            "Coverage",
            Map.of("insurance-buffer", "true", HttpHeaders.CONTENT_TYPE, "application/json"),
            postBody));
  }

  private Coverage insuranceBufferCreate() {
    return Coverage.builder()
        .status(Status.draft)
        .type(
            CodeableConcept.builder()
                .coding(
                    Coding.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.608012")
                        .code("1")
                        .build()
                        .asList())
                .build())
        .subscriber(
            Reference.builder()
                .reference("Patient/" + testIds.patient())
                .display("Sheriff Big-Boi")
                .build())
        .subscriberId("R50797108")
        .beneficiary(
            Reference.builder()
                .identifier(
                    Identifier.builder()
                        .type(
                            CodeableConcept.builder()
                                .coding(Coding.builder().code("MB").build().asList())
                                .build())
                        .value("1234")
                        .build())
                .reference("Patient/" + testIds.patient())
                .build())
        .relationship(
            CodeableConcept.builder()
                .coding(
                    Coding.builder()
                        .system("http://terminology.hl7.org/CodeSystem/subscriber-relationship")
                        .code("spouse")
                        .display("Spouse")
                        .build()
                        .asList())
                .text("Spouse")
                .build())
        .payor(Reference.builder().reference("#2").build().asList())
        .coverageClass(
            Coverage.CoverageClass.builder()
                .type(
                    CodeableConcept.builder()
                        .coding(
                            Coding.builder()
                                .system("http://terminology.hl7.org/CodeSystem/coverage-class")
                                .code("group")
                                .build()
                                .asList())
                        .build())
                .value("#1")
                .build()
                .asList())
        .contained(
            List.of(
                InsurancePlan.builder()
                    .id("1")
                    .identifier(
                        Identifier.builder()
                            .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.908002")
                            .value("GRP123456")
                            .build()
                            .asList())
                    .name("BCBS OF TX GROUP")
                    .build(),
                Organization.builder().id("2").active(true).name("BCBS OF FLORIDA").build()))
        .build();
  }

  @Test
  @Order(2)
  void read() {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    var path = "Coverage/{coverage}";
    verifyAll(
        test(200, Coverage.class, path, testIds.coverage()),
        test(404, OperationOutcome.class, path, "I3-404"));
  }

  void search() {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    verifyAll(
        test(
            200,
            Coverage.Bundle.class,
            R4TestSupport::atLeastOneEntry,
            "Coverage?patient={icn}",
            testIds.patient()));
  }
}
