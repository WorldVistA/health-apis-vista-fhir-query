package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static java.util.Collections.emptyList;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.InsurancePlan.Plan;
import gov.va.api.health.vistafhirquery.service.controller.ExtensionFactory;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4InsurancePlanTransformer {
  /** The insurance group fields needed by the transformer. */

  // The following list can be generated using:
  // grep GroupInsurancePlan R4InsurancePlanTransformer.java \
  //        | sed 's/.*\(GroupInsurancePlan\.[A-Z0-9_]\+\).*/\1,/' \
  //        | grep -vE '(import|FILE_NUMBER)' \
  //        | sort -u
  public static final List<String> REQUIRED_FIELDS =
      List.of(
          GroupInsurancePlan.AMBULATORY_CARE_CERTIFICATION,
          GroupInsurancePlan.BANKING_IDENTIFICATION_NUMBER,
          GroupInsurancePlan.BENEFITS_ASSIGNABLE_,
          GroupInsurancePlan.ELECTRONIC_PLAN_TYPE,
          GroupInsurancePlan.EXCLUDE_PRE_EXISTING_CONDITION,
          GroupInsurancePlan.GROUP_NAME,
          GroupInsurancePlan.GROUP_NUMBER,
          GroupInsurancePlan.INSURANCE_COMPANY,
          GroupInsurancePlan.IS_PRE_CERTIFICATION_REQUIRED_,
          GroupInsurancePlan.IS_UTILIZATION_REVIEW_REQUIRED,
          GroupInsurancePlan.PLAN_CATEGORY,
          GroupInsurancePlan.PLAN_ID,
          GroupInsurancePlan.PLAN_STANDARD_FTF,
          GroupInsurancePlan.PLAN_STANDARD_FTF_VALUE,
          GroupInsurancePlan.PROCESSOR_CONTROL_NUMBER_PCN_,
          GroupInsurancePlan.TYPE_OF_PLAN);

  static final Map<String, Boolean> YES_NO = Map.of("1", true, "0", false);

  @NonNull Map.Entry<String, LhsLighthouseRpcGatewayResponse.Results> rpcResults;

  private List<Extension> extensions(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    ExtensionFactory extensions = ExtensionFactory.of(entry, YES_NO);
    return Stream.of(
            extensions.ofYesNoBoolean(
                GroupInsurancePlan.IS_UTILIZATION_REVIEW_REQUIRED,
                "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired"),
            extensions.ofYesNoBoolean(
                GroupInsurancePlan.IS_PRE_CERTIFICATION_REQUIRED_,
                "http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired"),
            extensions.ofYesNoBoolean(
                GroupInsurancePlan.EXCLUDE_PRE_EXISTING_CONDITION,
                "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions"),
            extensions.ofYesNoBoolean(
                GroupInsurancePlan.BENEFITS_ASSIGNABLE_,
                "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable"),
            extensions.ofYesNoBoolean(
                GroupInsurancePlan.AMBULATORY_CARE_CERTIFICATION,
                "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare"),
            extensions.ofQuantity(
                GroupInsurancePlan.PLAN_STANDARD_FTF_VALUE,
                entry.external(GroupInsurancePlan.PLAN_STANDARD_FTF).orElse(null),
                "urn:oid:2.16.840.1.113883.3.8901.3.1.3558013",
                "http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame"))
        .filter(Objects::nonNull)
        .toList();
  }

  private Identifier identifier(Optional<String> value, String system) {
    return value.map(v -> Identifier.builder().value(v).system(system).build()).orElse(null);
  }

  private List<Identifier> identifiers(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return Stream.of(
            identifier(
                entry.external(GroupInsurancePlan.GROUP_NUMBER),
                "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002"),
            identifier(
                entry.external(GroupInsurancePlan.PLAN_ID),
                "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001"),
            identifier(
                entry.external(GroupInsurancePlan.BANKING_IDENTIFICATION_NUMBER),
                "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68002"),
            identifier(
                entry.external(GroupInsurancePlan.PROCESSOR_CONTROL_NUMBER_PCN_),
                "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68003"))
        .filter(Objects::nonNull)
        .toList();
  }

  private Reference ownedBy(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return entry
        .internal(GroupInsurancePlan.INSURANCE_COMPANY)
        .map(
            value ->
                RecordCoordinates.builder()
                    .site(site())
                    .file(InsuranceCompany.FILE_NUMBER)
                    .ien(value)
                    .build())
        .map(
            coords ->
                toReference(
                    "Organization", coords, entry.external(GroupInsurancePlan.INSURANCE_COMPANY)))
        .orElse(null);
  }

  private List<InsurancePlan.Plan> plan(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    CodeableConcept typeOfPlan =
        type(
            entry,
            GroupInsurancePlan.TYPE_OF_PLAN,
            "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009");
    if (typeOfPlan == null) {
      return emptyList();
    }
    return List.of(Plan.builder().type(typeOfPlan).build());
  }

  private String site() {
    return rpcResults.getKey();
  }

  /** Transform an RPC response to fhir. */
  public Stream<InsurancePlan> toFhir() {
    return rpcResults.getValue().results().stream()
        .filter(Objects::nonNull)
        .filter(r -> GroupInsurancePlan.FILE_NUMBER.equals(r.file()))
        .map(this::toInsurancePlan)
        .filter(Objects::nonNull);
  }

  private InsurancePlan toInsurancePlan(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    if (isBlank(entry.fields())) {
      return null;
    }
    return InsurancePlan.builder()
        .id(
            RecordCoordinates.builder()
                .site(rpcResults.getKey())
                .file(GroupInsurancePlan.FILE_NUMBER)
                .ien(entry.ien())
                .build()
                .toString())
        .meta(Meta.builder().source(site()).build())
        .ownedBy(ownedBy(entry))
        .plan(plan(entry))
        .type(types(entry))
        .name(entry.internal(GroupInsurancePlan.GROUP_NAME).orElse(null))
        .identifier(identifiers(entry))
        .extension(extensions(entry))
        .build();
  }

  private CodeableConcept type(
      LhsLighthouseRpcGatewayResponse.FilemanEntry entry, String fieldName, String system) {
    Optional<String> code = entry.internal(fieldName);
    Optional<String> display = entry.external(fieldName);
    if (isBlank(code)) {
      return null;
    }
    return asCodeableConcept(
        Coding.builder().code(code.get()).display(display.orElse(null)).system(system).build());
  }

  private List<CodeableConcept> types(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return Stream.of(
            type(
                entry,
                GroupInsurancePlan.PLAN_CATEGORY,
                "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8014"),
            type(
                entry,
                GroupInsurancePlan.ELECTRONIC_PLAN_TYPE,
                "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8015"))
        .filter(Objects::nonNull)
        .toList();
  }
}
