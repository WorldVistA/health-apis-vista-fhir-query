package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.service.controller.coverage.InsuranceBufferStructureDefinitions;
import gov.va.api.health.vistafhirquery.tests.CreateResourceVerifier;
import gov.va.api.health.vistafhirquery.tests.SystemDefinitions;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CoverageIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Test
  void createAndRead() {
    // Requires LHS LIGHTHOUSE RPC GATEWAY to be deployed to vista
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    CreateResourceVerifier.builder()
        .apiName("insurance-fhir")
        .serviceDefinition(SystemDefinitions.systemDefinition().basePath())
        .requestPath("/hcs/673/r4/Coverage")
        .requestBody(insuranceBufferCreate())
        .build()
        .test();
  }

  private Coverage insuranceBufferCreate() {
    var newSubscriberEveryTime = "R" + Instant.now().getEpochSecond();
    return Coverage.builder()
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
        .subscriberId(newSubscriberEveryTime)
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
                        .code("self")
                        .display("Self")
                        .build()
                        .asList())
                .text("Self")
                .build())
        .period(Period.builder().start("1992-01-12").end("2025-01-01").build())
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
                    .name("BCBS OF TX GROUP")
                    .extension(
                        List.of(
                            Extension.builder()
                                .url(
                                    InsuranceBufferStructureDefinitions.UTILIZATION_REVIEW_REQUIRED)
                                .valueBoolean(true)
                                .build(),
                            Extension.builder()
                                .url(InsuranceBufferStructureDefinitions.PRECERTIFICATION_REQUIRED)
                                .valueBoolean(true)
                                .build(),
                            Extension.builder()
                                .url(
                                    InsuranceBufferStructureDefinitions
                                        .AMBULATORY_CARE_CERTIFICATION)
                                .valueBoolean(true)
                                .build(),
                            Extension.builder()
                                .url(
                                    InsuranceBufferStructureDefinitions
                                        .EXCLUDE_PREEXISTING_CONDITION)
                                .valueBoolean(false)
                                .build(),
                            Extension.builder()
                                .url(InsuranceBufferStructureDefinitions.BENEFITS_ASSIGNABLE)
                                .valueBoolean(true)
                                .build()))
                    .identifier(
                        List.of(
                            Identifier.builder()
                                .system(InsuranceBufferStructureDefinitions.GROUP_NUMBER)
                                .value("GRP123456")
                                .build(),
                            Identifier.builder()
                                .system(
                                    InsuranceBufferStructureDefinitions
                                        .BANKING_IDENTIFICATION_NUMBER)
                                .value("88888888")
                                .build(),
                            Identifier.builder()
                                .system(
                                    InsuranceBufferStructureDefinitions
                                        .PROCESSOR_CONTROL_NUMBER_PCN)
                                .value("121212121212")
                                .build()))
                    .plan(
                        List.of(
                            InsurancePlan.Plan.builder()
                                .type(
                                    CodeableConcept.builder()
                                        .coding(
                                            List.of(
                                                Coding.builder()
                                                    .system(
                                                        InsuranceBufferStructureDefinitions
                                                            .TYPE_OF_PLAN)
                                                    .code("40")
                                                    .display(
                                                        "PREFERRED PROVIDER ORGANIZATION (PPO)")
                                                    .build()))
                                        .text("PREFERRED PROVIDER ORGANIZATION (PPO)")
                                        .build())
                                .build()))
                    .build(),
                Organization.builder()
                    .id("2")
                    .active(true)
                    .name("BCBS OF FLORIDA")
                    .address(
                        Address.builder()
                            .state("FLORIDA")
                            .city("JACKSONVILLE")
                            .line(List.of("PO BOX 1798", "REGIONAL CLAIMS OFFICE", "ATTN: MICHAEL"))
                            .postalCode("322310014")
                            .build()
                            .asList())
                    .contact(
                        List.of(
                            Organization.Contact.builder()
                                .telecom(
                                    List.of(
                                        ContactPoint.builder()
                                            .value("800-727-222")
                                            .system(ContactPoint.ContactPointSystem.phone)
                                            .build()))
                                .purpose(
                                    CodeableConcept.builder()
                                        .coding(
                                            Collections.singletonList(
                                                Coding.builder()
                                                    .system(
                                                        "http://terminology.hl7.org/CodeSystem/contactentity-type")
                                                    .code("BILL")
                                                    .display("BILL")
                                                    .build()))
                                        .build())
                                .build(),
                            Organization.Contact.builder()
                                .telecom(
                                    List.of(
                                        ContactPoint.builder()
                                            .value("800-955-5692")
                                            .system(ContactPoint.ContactPointSystem.phone)
                                            .build()))
                                .purpose(
                                    CodeableConcept.builder()
                                        .coding(
                                            Collections.singletonList(
                                                Coding.builder()
                                                    .system(
                                                        "https://va.gov/fhir/CodeSystem/organization-contactType")
                                                    .code("PRECERT")
                                                    .display("PRECERT")
                                                    .build()))
                                        .build())
                                .build()))
                    .telecom(
                        ContactPoint.builder()
                            .value("777-999-4444")
                            .system(ContactPoint.ContactPointSystem.phone)
                            .build()
                            .asList())
                    .extension(
                        Extension.builder()
                            .url(InsuranceBufferStructureDefinitions.REIMBURSE)
                            .valueCodeableConcept(
                                CodeableConcept.builder()
                                    .coding(
                                        Collections.singletonList(
                                            Coding.builder()
                                                .code("WILL REIMBURSE")
                                                .system(
                                                    InsuranceBufferStructureDefinitions
                                                        .REIMBURSE_URN_OID)
                                                .build()))
                                    .build())
                            .build()
                            .asList())
                    .build()))
        .build();
  }
}
