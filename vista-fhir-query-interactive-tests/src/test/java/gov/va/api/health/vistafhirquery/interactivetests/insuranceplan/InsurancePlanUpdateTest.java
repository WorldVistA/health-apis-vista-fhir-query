package gov.va.api.health.vistafhirquery.interactivetests.insuranceplan;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class InsurancePlanUpdateTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void insurancePlanWrite() {
    TestContext ctx = new InteractiveTestContext("InsurancePlanUpdate");
    ctx.update(
        InsurancePlan.builder()
            .resourceType("InsurancePlan")
            .id(ctx.property("insurancePlanId"))
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
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions")
                        .valueBoolean(false)
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable")
                        .valueBoolean(true)
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare")
                        .valueBoolean(true)
                        .build(),
                    Extension.builder()
                        .url(
                            "http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame")
                        .valueQuantity(
                            Quantity.builder()
                                .value(
                                    BigDecimal.valueOf(
                                        Double.parseDouble(ctx.property("quantityValue"))))
                                .unit(ctx.property("quantityUnit"))
                                .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558013")
                                .build())
                        .build()))
            .identifier(
                List.of(
                    Identifier.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002")
                        .value(ctx.property("identifier"))
                        .build(),
                    Identifier.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001")
                        .value("VA96554")
                        .build(),
                    Identifier.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68002")
                        .value("88888888")
                        .build(),
                    Identifier.builder()
                        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68003")
                        .value("121212121212")
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
