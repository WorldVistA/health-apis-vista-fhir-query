package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class CoverageCreateForPatientTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void coverageWrite() {
    InteractiveTestContext ctx = new InteractiveTestContext("CoverageCreateForPatient");
    ctx.create(
        Coverage.builder()
            .resourceType("Coverage")
            .extension(
                List.of(
                    Extension.builder()
                        .url("http://va.gov/fhir/StructureDefinition/coverage-pharmacyPersonCode")
                        .valueInteger(2)
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/coverage-stopPolicyFromBilling")
                        .valueBoolean(false)
                        .build()))
            .status(Coverage.Status.active)
            .subscriberId("R50797108")
            .beneficiary(
                Reference.builder()
                    .reference(ctx.property("beneficiary"))
                    .identifier(
                        Identifier.builder()
                            .type(
                                CodeableConcept.builder()
                                    .coding(Coding.builder().code("MB").build().asList())
                                    .build())
                            .value("1234")
                            .build())
                    .build())
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
            .payor(Reference.builder().reference(ctx.property("payor")).build().asList())
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
                    .value("InsurancePlan/I3-1JeCN3qnboBvfJAeuA5VVg")
                    .build()
                    .asList())
            .build());
  }
}
