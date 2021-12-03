package gov.va.api.health.vistafhirquery.interactivetests.coverage;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class CoverageCreateForPatientTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void coverageWrite() {
    TestContext ctx = new InteractiveTestContext("CoverageCreateForPatient");
    ctx.create(
        Coverage.builder()
            .extension(
                Extension.builder()
                    .url("http://va.gov/fhir/StructureDefinition/coverage-stopPolicyFromBilling")
                    .valueBoolean(false)
                    .build()
                    .asList())
            .status(Coverage.Status.active)
            .subscriberId("R50797108")
            .dependent("2")
            .beneficiary(
                ctx.urlsFor(Patient.class)
                    .reference(ctx.property("patient"))
                    .identifier(
                        Identifier.builder()
                            .type(
                                CodeableConcept.builder()
                                    .coding(Coding.builder().code("MB").build().asList())
                                    .build())
                            .value("1234")
                            .build()))
            .relationship(
                CodeableConcept.builder()
                    .coding(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/subscriber-relationship")
                            .code(ctx.property("relationship"))
                            .display(ctx.property("relationship"))
                            .build()
                            .asList())
                    .text("Spouse")
                    .build())
            .period(
                Period.builder().start("1992-01-12T05:00:00Z").end("2025-01-01T05:00:00Z").build())
            .payor(ctx.urlsFor(Organization.class).reference(ctx.property("payorId")).asList())
            .order(1)
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
                    .value("InsurancePlan/" + ctx.property("insurancePlanId"))
                    .build()
                    .asList())
            .build());
  }
}
