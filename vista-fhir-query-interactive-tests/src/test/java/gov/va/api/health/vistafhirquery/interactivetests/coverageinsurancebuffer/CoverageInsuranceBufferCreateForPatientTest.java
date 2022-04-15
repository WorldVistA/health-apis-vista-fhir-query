package gov.va.api.health.vistafhirquery.interactivetests.coverageinsurancebuffer;

import static gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem.phone;
import static java.lang.Integer.parseInt;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.RelatedPerson;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import gov.va.api.health.vistafhirquery.service.controller.coverage.InsuranceBufferDefinitions;
import gov.va.api.health.vistafhirquery.service.controller.coverage.InsuranceBufferStructureDefinitions;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class CoverageInsuranceBufferCreateForPatientTest {
  private ArrayList<Resource> containedResources(TestContext ctx) {
    ArrayList<Resource> contained = new ArrayList<>(3);
    contained.add(insurancePlan(ctx));
    contained.add(organization(ctx));
    if (!"self".equals(ctx.property("relationship"))) {
      contained.add(relatedPerson(ctx));
    }
    return contained;
  }

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void coverageBufferWrite() {
    TestContext ctx = new InteractiveTestContext("CoverageInsuranceBufferCreateForPatient");
    ctx.create(
        Coverage.builder()
            .contained(containedResources(ctx))
            .status(Enum.valueOf(Coverage.Status.class, ctx.property("status")))
            .type(
                CodeableConcept.builder()
                    .coding(
                        Coding.builder()
                            .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.8808001")
                            .code("1")
                            .build()
                            .asList())
                    .build())
            .subscriber(
                "self".equals(ctx.property("relationship"))
                    ? null
                    : Reference.builder().reference("#3").display("Bobby Test").build())
            .subscriberId(ctx.property("subscriberId"))
            .beneficiary(
                ctx.urlsFor(Patient.class)
                    .reference(ctx.property("beneficiaryId"))
                    .identifier(
                        Identifier.builder()
                            .type(
                                CodeableConcept.builder()
                                    .coding(
                                        Coding.builder()
                                            .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                            .code("MB")
                                            .build()
                                            .asList())
                                    .build())
                            .value("1234")
                            .build()))
            .dependent(ctx.property("dependent"))
            .relationship(
                CodeableConcept.builder()
                    .coding(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/subscriber-relationship")
                            .code(ctx.property("relationship"))
                            .display(ctx.property("relationship"))
                            .build()
                            .asList())
                    .text(ctx.property("relationship"))
                    .build())
            .period(
                Period.builder()
                    .start(ctx.property("periodStart"))
                    .end(ctx.property("periodEnd"))
                    .build())
            .payor(Reference.builder().reference("#2").build().asList())
            .coverageClass(
                Coverage.CoverageClass.builder()
                    .type(
                        CodeableConcept.builder()
                            .coding(
                                Coding.builder()
                                    .system("http://terminology.hl7.org/CodeSystem/coverage-class")
                                    .code(ctx.property("coverageClass"))
                                    .build()
                                    .asList())
                            .build())
                    .value("#1")
                    .build()
                    .asList())
            .order(parseInt(ctx.property("order")))
            .build());
  }

  private InsurancePlan insurancePlan(TestContext ctx) {
    return InsurancePlan.builder()
        .id("1")
        .extension(
            List.of(
                Extension.builder()
                    .url(InsuranceBufferStructureDefinitions.UTILIZATION_REVIEW_REQUIRED)
                    .valueBoolean(Boolean.valueOf(ctx.property("utilizationReviewRequired")))
                    .build(),
                Extension.builder()
                    .url(InsuranceBufferStructureDefinitions.PRECERTIFICATION_REQUIRED)
                    .valueBoolean(Boolean.valueOf(ctx.property("precertificationRequired")))
                    .build(),
                Extension.builder()
                    .url(InsuranceBufferStructureDefinitions.AMBULATORY_CARE_CERTIFICATION)
                    .valueBoolean(Boolean.valueOf(ctx.property("ambulatoryCareCertification")))
                    .build(),
                Extension.builder()
                    .url(InsuranceBufferStructureDefinitions.EXCLUDE_PREEXISTING_CONDITION)
                    .valueBoolean(Boolean.valueOf(ctx.property("excludePreexistingConditions")))
                    .build(),
                Extension.builder()
                    .url(InsuranceBufferStructureDefinitions.BENEFITS_ASSIGNABLE)
                    .valueBoolean(Boolean.valueOf(ctx.property("benefitsAssignable")))
                    .build()))
        .identifier(
            List.of(
                Identifier.builder()
                    .system(InsuranceBufferDefinitions.get().groupNumber().system())
                    .value(ctx.property("groupNumber"))
                    .build(),
                Identifier.builder()
                    .system(InsuranceBufferDefinitions.get().bankingIdentificationNumber().system())
                    .value(ctx.property("bankingIdentificationNumber"))
                    .build(),
                Identifier.builder()
                    .system(InsuranceBufferDefinitions.get().processorControlNumber().system())
                    .value(ctx.property("processorControlNumberPcn"))
                    .build()))
        .name(ctx.property("insurancePlanName"))
        .plan(
            InsurancePlan.Plan.builder()
                .type(
                    CodeableConcept.builder()
                        .coding(
                            Coding.builder()
                                .system(InsuranceBufferDefinitions.get().typeOfPlan().valueSet())
                                .code(ctx.property("insurancePlanCode"))
                                .display(ctx.property("insurancePlanDisplayText"))
                                .build()
                                .asList())
                        .text(ctx.property("insurancePlanDisplayText"))
                        .build())
                .build()
                .asList())
        .build();
  }

  private Organization organization(TestContext ctx) {
    return Organization.builder()
        .id("2")
        .active(Boolean.TRUE)
        .extension(
            Extension.builder()
                .url(InsuranceBufferDefinitions.get().reimburse().structureDefinition())
                .valueCodeableConcept(
                    CodeableConcept.builder()
                        .coding(
                            Coding.builder()
                                .system(
                                    InsuranceBufferDefinitions.get()
                                        .reimburse()
                                        .valueDefinition()
                                        .valueSet())
                                .code(ctx.property("willReimburseForCare"))
                                .build()
                                .asList())
                        .build())
                .build()
                .asList())
        .name(ctx.property("organizationName"))
        .telecom(
            ContactPoint.builder()
                .system(phone)
                .value(ctx.property("organizationPhoneNumber"))
                .build()
                .asList())
        .address(
            Address.builder()
                .line(
                    List.of(
                        ctx.property("organizationAddressLine1"),
                        ctx.property("organizationAddressLine2"),
                        ctx.property("organizationAddressLine3")))
                .city(ctx.property("organizationAddressCity"))
                .state(ctx.property("organizationAddressState"))
                .postalCode(ctx.property("organizationAddressPostalCode"))
                .build()
                .asList())
        .contact(
            List.of(
                Organization.Contact.builder()
                    .extension(
                        Extension.builder()
                            .url(
                                "http://hl7.org/fhir/us/davinci-pdex-plan-net/StructureDefinition/via-intermediary")
                            .valueReference(Reference.builder().display("BILLING").build())
                            .build()
                            .asList())
                    .purpose(
                        CodeableConcept.builder()
                            .coding(
                                Coding.builder()
                                    .system(
                                        "http://terminology.hl7.org/CodeSystem/contactentity-type")
                                    .code("BILL")
                                    .display("BILL")
                                    .build()
                                    .asList())
                            .text("BILL")
                            .build())
                    .telecom(
                        ContactPoint.builder()
                            .system(phone)
                            .value(ctx.property("organizationBillingPhoneNumber"))
                            .build()
                            .asList())
                    .build(),
                Organization.Contact.builder()
                    .purpose(
                        CodeableConcept.builder()
                            .coding(
                                Coding.builder()
                                    .system(
                                        "https://va.gov/fhir/CodeSystem/organization-contactType")
                                    .code("PRECERT")
                                    .display("PRECERT")
                                    .build()
                                    .asList())
                            .text("PRECERT")
                            .build())
                    .telecom(
                        ContactPoint.builder()
                            .system(phone)
                            .value(ctx.property("organizationPrecertPhoneNumber"))
                            .build()
                            .asList())
                    .build()))
        .build();
  }

  private RelatedPerson relatedPerson(TestContext ctx) {
    return RelatedPerson.builder()
        .id("#3")
        .extension(
            Extension.builder()
                .url(InsuranceBufferDefinitions.get().insuredsSex().structureDefinition())
                .valueCode(ctx.property("relatedPersonSex"))
                .build()
                .asList())
        .identifier(
            Identifier.builder()
                .use(Identifier.IdentifierUse.official)
                .type(
                    CodeableConcept.builder()
                        .coding(
                            Coding.builder()
                                .system("http://hl7.org/fhir/v2/0203")
                                .code("SB")
                                .build()
                                .asList())
                        .build())
                .system("http://hl7.org/fhir/sid/us-ssn")
                .value(ctx.property("relatedPersonSsn"))
                .assigner(
                    Reference.builder().display("United States Social Security Number").build())
                .build()
                .asList())
        .patient(ctx.urlsFor(Patient.class).reference(ctx.property("relatedPersonPatientId")))
        .name(HumanName.builder().text(ctx.property("relatedPersonName")).build().asList())
        .telecom(
            ContactPoint.builder()
                .system(phone)
                .value(ctx.property("relatedPersonPhoneNumber"))
                .build()
                .asList())
        .birthDate(ctx.property("relatedPersonBirthdate"))
        .address(
            Address.builder()
                .line(
                    List.of(
                        ctx.property("relatedPersonAddressLine1"),
                        ctx.property("relatedPersonAddressLine2")))
                .city(ctx.property("relatedPersonAddressCity"))
                .state(ctx.property("relatedPersonAddressState"))
                .postalCode(ctx.property("relatedPersonAddressPostalCode"))
                .build()
                .asList())
        .build();
  }
}
