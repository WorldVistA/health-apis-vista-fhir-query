package gov.va.api.health.vistafhirquery.interactivetests.insuranceplan;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.math.BigDecimal.valueOf;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class InsurancePlanCreateTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void insurancePlanWrite() {
    TestContext ctx = new InteractiveTestContext("InsurancePlanCreate");
    ctx.create(
        InsurancePlan.builder()
            .resourceType("InsurancePlan")
            .extension(
                List.of(
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired")
                        .valueBoolean(parseBoolean(ctx.property("isUtilizationReviewRequired")))
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired")
                        .valueBoolean(parseBoolean(ctx.property("isPreCertificationRequired")))
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions")
                        .valueBoolean(parseBoolean(ctx.property("excludePreexistingConditions")))
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable")
                        .valueBoolean(parseBoolean(ctx.property("areBenefitsAssignable")))
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare")
                        .valueBoolean(
                            parseBoolean(ctx.property("isCertificationRequiredForAmbulatoryCare")))
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame")
                        .valueQuantity(
                            Quantity.builder()
                                .value(valueOf(parseDouble(ctx.property("quantityValue"))))
                                .unit(ctx.property("quantityUnit"))
                                .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558013")
                                .build())
                        .build()))
            .identifier(
                List.of(
                    Identifier.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002")
                        .value(ctx.property("groupNumber"))
                        .build(),
                    Identifier.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001")
                        .value(ctx.property("planId"))
                        .build(),
                    Identifier.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68002")
                        .value(ctx.property("bankingIdentificationNumber"))
                        .build(),
                    Identifier.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68003")
                        .value(ctx.property("processorControlNumber"))
                        .build()))
            .type(
                CodeableConcept.builder()
                    .coding(
                        Coding.builder()
                            .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8015")
                            .code(ctx.property("type"))
                            .build()
                            .asList())
                    .build()
                    .asList())
            .name(ctx.property("name"))
            .ownedBy(
                Reference.builder().reference("Organization/" + ctx.property("ownedBy")).build())
            .plan(
                InsurancePlan.Plan.builder()
                    .type(
                        CodeableConcept.builder()
                            .coding(
                                Coding.builder()
                                    .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009")
                                    .display(ctx.property("planType"))
                                    .build()
                                    .asList())
                            .build())
                    .build()
                    .asList())
            .build());
  }
}
