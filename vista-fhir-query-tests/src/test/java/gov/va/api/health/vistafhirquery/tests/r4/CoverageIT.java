package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

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
    assumeEnvironmentIn(Environment.QA, Environment.STAGING_LAB, Environment.LAB);
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
        .period(Period.builder().start("1992-01-12T05:00:00Z").end("2025-01-01T05:00:00Z").build())
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
                                    "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare")
                                .valueBoolean(true)
                                .build(),
                            Extension.builder()
                                .url(
                                    "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions")
                                .valueBoolean(false)
                                .build(),
                            Extension.builder()
                                .url(
                                    "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable")
                                .valueBoolean(true)
                                .build()))
                    .identifier(
                        List.of(
                            Identifier.builder()
                                .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.908002")
                                .value("GRP123456")
                                .build(),
                            Identifier.builder()
                                .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.40801")
                                .value("88888888")
                                .build(),
                            Identifier.builder()
                                .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.408011")
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
                                                        "urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.408009")
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
                            .url(
                                "http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare")
                            .valueCodeableConcept(
                                CodeableConcept.builder()
                                    .coding(
                                        Collections.singletonList(
                                            Coding.builder()
                                                .code("WILL REIMBURSE")
                                                .system(
                                                    "urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.208005")
                                                .build()))
                                    .build())
                            .build()
                            .asList())
                    .build()))
        .build();
  }
}
