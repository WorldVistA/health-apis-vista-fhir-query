package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.CreateResourceVerifier;
import gov.va.api.health.vistafhirquery.tests.SystemDefinitions;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CoverageIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Test
  void createAndRead() {
    assumeEnvironmentIn(Environment.QA, Environment.STAGING_LAB, Environment.LAB);
    CreateResourceVerifier.<Coverage>builder()
        .apiName("insurance-fhir")
        .serviceDefinition(SystemDefinitions.systemDefinition().basePath())
        .requestPath("/hcs/673/r4/Coverage")
        .requestBody(insuranceBufferCreate())
        .build()
        .test();
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
}
