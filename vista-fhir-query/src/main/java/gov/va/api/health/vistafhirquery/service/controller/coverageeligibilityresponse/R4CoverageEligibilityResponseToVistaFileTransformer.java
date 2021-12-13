package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.fhir.api.FhirDateTime.parseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.nullOrfalse;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.patientCoordinatesForReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.recordCoordinatesForReference;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static java.util.function.Function.identity;

import gov.va.api.health.fhir.api.FhirDateTime;
import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Money;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Benefit;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Insurance;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Item;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Outcome;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Purpose;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Status;
import gov.va.api.health.vistafhirquery.service.controller.FilemanFactoryRegistry;
import gov.va.api.health.vistafhirquery.service.controller.FilemanIndexRegistry;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExactlyOneOfFields;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidConditionalField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidReferenceId;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.CodeableConceptExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ComplexExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.DateTimeExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.DecimalExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.PeriodExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.QuantityExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.StringExtensionHandler;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.EligibilityBenefit;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.HealthCareCodeInformation;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.HealthcareServicesDelivery;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.IivResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LimitationComment;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.ServiceTypes;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberAdditionalInfo;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberDates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberReferenceId;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageEligibilityResponseToVistaFileTransformer {
  FilemanFactoryRegistry factoryRegistry;

  FilemanIndexRegistry indexRegistry;

  CoverageEligibilityResponse coverageEligibilityResponse;

  ZoneId zoneId;

  @Builder
  R4CoverageEligibilityResponseToVistaFileTransformer(
      @NonNull CoverageEligibilityResponse coverageEligibilityResponse, ZoneId timezone) {
    this.coverageEligibilityResponse = coverageEligibilityResponse;
    this.zoneId = timezone == null ? ZoneId.of("UTC") : timezone;
    this.factoryRegistry = FilemanFactoryRegistry.create();
    this.indexRegistry = FilemanIndexRegistry.create();
  }

  private Stream<WriteableFilemanValue> benefit(Benefit benefit) {
    Set<WriteableFilemanValue> filemanValues = new HashSet<>();
    filemanValues.add(
        factoryRegistry()
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredCodeableConcept(
                EligibilityBenefit.ELIGIBILITY_BENEFIT_INFO,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
                benefit.type(),
                CoverageEligibilityResponseStructureDefinitions.ELIGIBILITY_BENEFIT_INFO));
    filemanValues.addAll(benefitExtensionProcessor().process(benefit.extension()));
    filemanValues.add(money(benefit.allowedMoney(), benefit.usedMoney()));
    return filemanValues.stream();
  }

  private R4ExtensionProcessor benefitExtensionProcessor() {
    return R4ExtensionProcessor.of(
        ".insurance[].item[].benefit[].extension[]",
        QuantityExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.BENEFIT_QUANTITY)
            .required(REQUIRED)
            .filemanFactory(factoryRegistry().get(EligibilityBenefit.FILE_NUMBER))
            .index(indexRegistry().get(EligibilityBenefit.FILE_NUMBER))
            .valueFieldNumber(EligibilityBenefit.QUANTITY)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.BENEFIT_QUANTITY_CODE)
            .required(REQUIRED)
            .filemanFactory(factoryRegistry().get(EligibilityBenefit.FILE_NUMBER))
            .index(indexRegistry().get(EligibilityBenefit.FILE_NUMBER))
            .fieldNumber(EligibilityBenefit.QUANTITY_QUALIFIER)
            .codingSystem(
                CoverageEligibilityResponseStructureDefinitions.BENEFIT_QUANTITY_CODE_SYSTEM)
            .build(),
        QuantityExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.BENEFIT_PERCENT)
            .required(REQUIRED)
            .filemanFactory(factoryRegistry.get(EligibilityBenefit.FILE_NUMBER))
            .index(indexRegistry.get(EligibilityBenefit.FILE_NUMBER))
            .valueFieldNumber(EligibilityBenefit.PERCENT)
            .build());
  }

  String coverageStatus(Boolean status, Boolean anyExcluded) {
    if (nullOrfalse(status)) {
      return "NOT COVERED";
    }
    if (nullOrfalse(anyExcluded)) {
      return "COVERED";
    }
    return "CONDITIONAL COVERAGE";
  }

  WriteableFilemanValue dateTimePeriod(Period period) {
    if (isBlank(period)) {
      throw MissingRequiredField.builder().jsonPath(".insurance[].benefitPeriod").build();
    }
    if (isBlank(period.start())) {
      throw MissingRequiredField.builder().jsonPath(".insurance[].benefitPeriod.start").build();
    }
    var formatter = DateTimeFormatter.ofPattern("MMddyyyy").withZone(zoneId());
    var vistaDateTimePeriod = formatter.format(parseDateTime(period.start()));
    if (!isBlank(period.end())) {
      var formattedEndDate = formatter.format(parseDateTime(period.end()));
      vistaDateTimePeriod = vistaDateTimePeriod + "-" + formattedEndDate;
    }
    return factoryRegistry()
        .get(IivResponse.FILE_NUMBER)
        .forRequiredString(
            IivResponse.DATE_TIME_PERIOD,
            indexRegistry().get(IivResponse.FILE_NUMBER),
            vistaDateTimePeriod);
  }

  private List<WriteableFilemanValue> description(String description, Boolean excluded) {
    Optional<WriteableFilemanValue> limitationComment =
        factoryRegistry()
            .get(LimitationComment.FILE_NUMBER)
            .forOptionalString(
                LimitationComment.LIMITATION_COMMENT,
                indexRegistry().get(PlanCoverageLimitations.FILE_NUMBER),
                description);
    if (!nullOrfalse(excluded) && limitationComment.isEmpty()) {
      throw InvalidConditionalField.builder()
          .jsonPath("insurance[0].item[].description")
          .condition("Must be populated when insurance[0].item[].excluded is true.")
          .build();
    }
    if (limitationComment.isEmpty()) {
      return Collections.emptyList();
    }
    return List.of(
        limitationComment.get(),
        factoryRegistry
            .get(LimitationComment.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                indexRegistry.getAndIncrement(LimitationComment.FILE_NUMBER),
                PlanCoverageLimitations.FILE_NUMBER,
                indexRegistry.get(PlanCoverageLimitations.FILE_NUMBER)));
  }

  private R4ExtensionProcessor extensionProcessor() {
    return R4ExtensionProcessor.of(
        ".extension[]",
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_INFO_STATUS_CODE_DEFINITION)
            .filemanFactory(factoryRegistry().get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_INFO_STATUS_CODE)
            .index(indexRegistry().get(IivResponse.FILE_NUMBER))
            .codingSystem(CoverageEligibilityResponseStructureDefinitions.MILITARY_INFO_STATUS_CODE)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_EMPLOYMENT_STATUS_DEFINITION)
            .filemanFactory(factoryRegistry().get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_EMPLOYMENT_STATUS)
            .index(indexRegistry().get(IivResponse.FILE_NUMBER))
            .codingSystem(
                CoverageEligibilityResponseStructureDefinitions.MILITARY_EMPLOYMENT_STATUS)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_GOVT_AFFILIATION_CODE_DEFINITION)
            .filemanFactory(factoryRegistry().get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_GOVT_AFFILIATION_CODE)
            .index(indexRegistry().get(IivResponse.FILE_NUMBER))
            .codingSystem(
                CoverageEligibilityResponseStructureDefinitions.MILITARY_GOVT_AFFILIATION_CODE)
            .required(REQUIRED)
            .build(),
        StringExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_PERSONNEL_DESCRIPTION_DEFINITION)
            .filemanFactory(factoryRegistry().get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_PERSONNEL_DESCRIPTION)
            .index(indexRegistry().get(IivResponse.FILE_NUMBER))
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_SERVICE_RANK_CODE_DEFINITION)
            .filemanFactory(factoryRegistry().get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_SERVICE_RANK_CODE)
            .index(indexRegistry().get(IivResponse.FILE_NUMBER))
            .codingSystem(
                CoverageEligibilityResponseStructureDefinitions.MILITARY_SERVICE_RANK_CODE)
            .required(REQUIRED)
            .build(),
        ComplexExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.HEALTH_CARE_CODE_DEFINITION)
            .required(REQUIRED)
            .childExtensions(
                List.of(
                    CodeableConceptExtensionHandler.forDefiningUrl("diagnosisCode")
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthCareCodeInformation.FILE_NUMBER))
                        .fieldNumber(HealthCareCodeInformation.DIAGNOSIS_CODE)
                        .index(indexRegistry().get(HealthCareCodeInformation.FILE_NUMBER))
                        .codingSystems(
                            List.of(
                                "http://hl7.org/fhir/sid/icd-9-cm",
                                "http://www.cms.gov/Medicare/Coding/ICD9",
                                "http://hl7.org/fhir/sid/icd-10-cm",
                                "http://www.cms.gov/Medicare/Coding/ICD10"))
                        .build(),
                    StringExtensionHandler.forDefiningUrl("diagnosisCodeQualifier")
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthCareCodeInformation.FILE_NUMBER))
                        .fieldNumber(HealthCareCodeInformation.DIAGNOSIS_CODE_QUALIFIER)
                        .index(indexRegistry().get(HealthCareCodeInformation.FILE_NUMBER))
                        .build(),
                    StringExtensionHandler.forDefiningUrl("primaryOrSecondary")
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthCareCodeInformation.FILE_NUMBER))
                        .fieldNumber(HealthCareCodeInformation.PRIMARY_OR_SECONDARY)
                        .index(indexRegistry().get(HealthCareCodeInformation.FILE_NUMBER))
                        .build()))
            .build());
  }

  WriteableFilemanValue identifier(@NonNull Identifier identifier) {
    if (isBlank(identifier.type()) || isBlank(identifier.type().text())) {
      throw MissingRequiredField.builder().jsonPath(".identifier[].type.text").build();
    }
    switch (identifier.type().text()) {
      case "MSH-10":
        return factoryRegistry()
            .get(IivResponse.FILE_NUMBER)
            .forRequiredIdentifier(
                IivResponse.MESSAGE_CONTROL_ID,
                indexRegistry().get(IivResponse.FILE_NUMBER),
                identifier);
      case "MSA-3":
        return factoryRegistry()
            .get(IivResponse.FILE_NUMBER)
            .forRequiredIdentifier(
                IivResponse.TRACE_NUMBER, indexRegistry().get(IivResponse.FILE_NUMBER), identifier);
      default:
        throw UnexpectedValueForField.builder()
            .jsonPath(".identifier[].type.text")
            .supportedValues(List.of("MSH-10", "MSA-3"))
            .valueReceived(identifier.type().text())
            .build();
    }
  }

  private String ienForPayerFileOrDie(RecordCoordinates coordinates) {
    if (!Payer.FILE_NUMBER.equals(coordinates.file())) {
      throw InvalidReferenceId.builder().jsonPath(".insurer").referenceType("Organization").build();
    }
    return coordinates.ien();
  }

  private Stream<WriteableFilemanValue> insurance(Insurance insurance) {
    Set<WriteableFilemanValue> filemanValues = new HashSet<>();
    insurance.item().stream().flatMap(this::item).forEach(filemanValues::add);
    filemanValues.add(dateTimePeriod(insurance.benefitPeriod()));
    filemanValues.addAll(insuranceExtensionProcessor().process(insurance.extension()));
    Boolean anyExcluded = insurance.item().stream().anyMatch(i -> !nullOrfalse(i.excluded()));
    filemanValues.add(
        factoryRegistry()
            .get(PlanCoverageLimitations.FILE_NUMBER)
            .forRequiredBoolean(
                PlanCoverageLimitations.COVERAGE_STATUS,
                indexRegistry().get(PlanCoverageLimitations.FILE_NUMBER),
                insurance.inforce(),
                status -> coverageStatus(status, anyExcluded)));
    patientCoordinatesForReference(insurance.coverage())
        .map(
            id ->
                WriteableFilemanValue.builder()
                    .file(InsuranceType.FILE_NUMBER)
                    .index(indexRegistry().getAndIncrement(InsuranceType.FILE_NUMBER))
                    .field("IEN")
                    .value(id.ien())
                    .build())
        .ifPresentOrElse(
            filemanValues::add,
            () -> {
              throw MissingRequiredField.builder().jsonPath(".insurance[].coverage").build();
            });
    return filemanValues.stream();
  }

  private R4ExtensionProcessor insuranceExtensionProcessor() {
    return R4ExtensionProcessor.of(
        ".insurance[].extension[]",
        DateTimeExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.EFFECTIVE_DATE)
            .required(REQUIRED)
            .filemanFactory(factoryRegistry().get(PlanCoverageLimitations.FILE_NUMBER))
            .dateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(zoneId()))
            .dateTimeFieldNumber(PlanCoverageLimitations.EFFECTIVE_DATE)
            .index(indexRegistry().get(PlanCoverageLimitations.FILE_NUMBER))
            .build());
  }

  private Stream<WriteableFilemanValue> item(Item item) {
    Set<WriteableFilemanValue> filemanValues = new HashSet<>();
    filemanValues.add(
        factoryRegistry()
            .get(ServiceTypes.FILE_NUMBER)
            .forRequiredCodeableConcept(
                ServiceTypes.SERVICE_TYPES,
                indexRegistry().get(ServiceTypes.FILE_NUMBER),
                item.category(),
                CoverageEligibilityResponseStructureDefinitions.SERVICE_TYPES));
    filemanValues.add(
        factoryRegistry()
            .get(ServiceTypes.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                indexRegistry().getAndIncrement(ServiceTypes.FILE_NUMBER),
                EligibilityBenefit.FILE_NUMBER,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER)));
    filemanValues.addAll(itemExtensionProcessor().process(item.extension()));
    filemanValues.add(
        factoryRegistry()
            .get(HealthcareServicesDelivery.FILE_NUMBER)
            .forRequiredInteger(
                HealthcareServicesDelivery.SEQUENCE,
                indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER),
                indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(HealthcareServicesDelivery.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                indexRegistry().getAndIncrement(HealthcareServicesDelivery.FILE_NUMBER),
                EligibilityBenefit.FILE_NUMBER,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(SubscriberAdditionalInfo.FILE_NUMBER)
            .forRequiredInteger(
                SubscriberAdditionalInfo.SEQUENCE,
                indexRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER),
                indexRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(SubscriberAdditionalInfo.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                indexRegistry().getAndIncrement(SubscriberAdditionalInfo.FILE_NUMBER),
                EligibilityBenefit.FILE_NUMBER,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(SubscriberReferenceId.FILE_NUMBER)
            .forRequiredInteger(
                SubscriberReferenceId.SEQUENCE,
                indexRegistry().get(SubscriberReferenceId.FILE_NUMBER),
                indexRegistry().get(SubscriberReferenceId.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(SubscriberDates.FILE_NUMBER)
            .forRequiredInteger(
                SubscriberDates.SEQUENCE,
                indexRegistry().get(SubscriberDates.FILE_NUMBER),
                indexRegistry().get(SubscriberDates.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(SubscriberReferenceId.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                indexRegistry().getAndIncrement(SubscriberReferenceId.FILE_NUMBER),
                EligibilityBenefit.FILE_NUMBER,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(SubscriberDates.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                indexRegistry().getAndIncrement(SubscriberDates.FILE_NUMBER),
                EligibilityBenefit.FILE_NUMBER,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredCodeableConcept(
                EligibilityBenefit.COVERAGE_LEVEL,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
                item.unit(),
                CoverageEligibilityResponseStructureDefinitions.ITEM_UNIT));
    filemanValues.add(
        factoryRegistry()
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredCodeableConcept(
                EligibilityBenefit.TIME_PERIOD_QUALIFIER,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
                item.term(),
                CoverageEligibilityResponseStructureDefinitions.ITEM_TERM));
    item.benefit().stream().flatMap(this::benefit).forEach(filemanValues::add);
    filemanValues.addAll(procedureCoding(item.productOrService()));
    filemanValues.add(procedureModifier(item.modifier()));
    filemanValues.add(
        factoryRegistry()
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredBoolean(
                EligibilityBenefit.AUTHORIZATION_CERTIFICATION,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
                item.authorizationRequired(),
                this::x12YesNo));
    filemanValues.add(
        factoryRegistry()
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredCodeableConcept(
                EligibilityBenefit.IN_PLAN,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
                item.network(),
                CoverageEligibilityResponseStructureDefinitions.X12_YES_NO_SYSTEM));
    filemanValues.add(
        factoryRegistry()
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredInteger(
                EligibilityBenefit.EB_NUMBER,
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
                indexRegistry().get(EligibilityBenefit.FILE_NUMBER)));
    filemanValues.add(
        factoryRegistry()
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                indexRegistry().getAndIncrement(EligibilityBenefit.FILE_NUMBER),
                IivResponse.FILE_NUMBER,
                indexRegistry().get(IivResponse.FILE_NUMBER)));
    filemanValues.addAll(description(item.description(), item.excluded()));
    return filemanValues.stream();
  }

  private R4ExtensionProcessor itemExtensionProcessor() {
    return R4ExtensionProcessor.of(
        ".insurance[].item[].extension[]",
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.REQUESTED_SERVICE_TYPE)
            .required(REQUIRED)
            .fieldNumber(InsuranceType.REQUESTED_SERVICE_TYPE)
            .filemanFactory(factoryRegistry.get(InsuranceType.FILE_NUMBER))
            .index(indexRegistry.get(InsuranceType.FILE_NUMBER))
            .codingSystem(
                CoverageEligibilityResponseStructureDefinitions.REQUESTED_SERVICE_TYPE_SYSTEM)
            .build(),
        ComplexExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE)
            .required(REQUIRED)
            .childExtensions(
                List.of(
                    PeriodExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE_PERIOD)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberDates.FILE_NUMBER))
                        .dateTimeFormatter(
                            DateTimeFormatter.ofPattern("MMddyyyy").withZone(zoneId()))
                        .periodStartFieldNumber(SubscriberDates.DATE)
                        .periodEndFieldNumber(SubscriberDates.DATE)
                        .index(indexRegistry().get(SubscriberDates.FILE_NUMBER))
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE_KIND)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberDates.FILE_NUMBER))
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_DATE_QUALIFIER)
                        .fieldNumber(SubscriberDates.DATE_QUALIFIER)
                        .index(indexRegistry().get(SubscriberDates.FILE_NUMBER))
                        .build()))
            .build(),
        ComplexExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_REFERENCE_ID_DEFINITION)
            .required(REQUIRED)
            .childExtensions(
                List.of(
                    StringExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_REFERENCE_ID_VALUE_DEFINITION)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberReferenceId.FILE_NUMBER))
                        .index(indexRegistry().get(SubscriberReferenceId.FILE_NUMBER))
                        .fieldNumber(SubscriberReferenceId.REFERENCE_ID)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_REFERENCE_ID_QUALIFIER_DEFINITION)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberReferenceId.FILE_NUMBER))
                        .index(indexRegistry().get(SubscriberReferenceId.FILE_NUMBER))
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_REFERENCE_ID_QUALIFIER)
                        .fieldNumber(SubscriberReferenceId.REFERENCE_ID_QUALIFIER)
                        .build(),
                    StringExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_REFERENCE_ID_DESCRIPTION_DEFINITION)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberReferenceId.FILE_NUMBER))
                        .index(indexRegistry().get(SubscriberReferenceId.FILE_NUMBER))
                        .fieldNumber(SubscriberReferenceId.DESCRIPTION)
                        .build()))
            .build(),
        ComplexExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .SUBSCRIBER_ADDITIONAL_INFO_DEFINITION)
            .required(REQUIRED)
            .childExtensions(
                List.of(
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_PLACE_OF_SERVICE_DEFINITION)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .index(indexRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .fieldNumber(SubscriberAdditionalInfo.PLACE_OF_SERVICE)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_PLACE_OF_SERVICE_SYSTEM)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_QUALIFIER_DEFINITION)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .index(indexRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .fieldNumber(SubscriberAdditionalInfo.QUALIFIER)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_QUALIFIER_SYSTEM)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_INJURY_CODE_DEFINITION)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .index(indexRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .fieldNumber(SubscriberAdditionalInfo.NATURE_OF_INJURY_CODE)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_INJURY_CODE_SYSTEM)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_INJURY_CATEGORY_DEFINITION)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .index(indexRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .fieldNumber(SubscriberAdditionalInfo.NATURE_OF_INJURY_CATEGORY)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_INJURY_CATEGORY_SYSTEM)
                        .build(),
                    StringExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_INJURY_TEXT_DEFINITION)
                        .required(REQUIRED)
                        .filemanFactory(factoryRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .index(indexRegistry().get(SubscriberAdditionalInfo.FILE_NUMBER))
                        .fieldNumber(SubscriberAdditionalInfo.NATURE_OF_INJURY_TEXT)
                        .build()))
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.COVERAGE_CATEGORY)
            .required(REQUIRED)
            .filemanFactory(factoryRegistry().get(PlanCoverageLimitations.FILE_NUMBER))
            .index(indexRegistry().get(PlanCoverageLimitations.FILE_NUMBER))
            .fieldNumber(PlanCoverageLimitations.COVERAGE_CATEGORY)
            .codingSystem(CoverageEligibilityResponseStructureDefinitions.COVERAGE_CATEGORY_SYSTEM)
            .build(),
        ComplexExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.HEALTHCARE_SERVICES_DELIVERY)
            .required(REQUIRED)
            .childExtensions(
                List.of(
                    QuantityExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.BENEFIT_QUANTITY)
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .index(indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .valueFieldNumber(HealthcareServicesDelivery.BENEFIT_QUANTITY)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.QUANTITY_QUALIFIER)
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .index(indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .fieldNumber(HealthcareServicesDelivery.QUANTITY_QUALIFIER)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .QUANTITY_QUALIFIER_SYSTEM)
                        .build(),
                    StringExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions
                                .SAMPLE_SELECTION_MODULUS)
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .index(indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .fieldNumber(HealthcareServicesDelivery.SAMPLE_SELECTION_MODULUS)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.UNITS_OF_MEASUREMENT)
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .index(indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .fieldNumber(HealthcareServicesDelivery.UNITS_OF_MEASUREMENT)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .UNITS_OF_MEASUREMENT_SYSTEM)
                        .build(),
                    DecimalExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.TIME_PERIODS)
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .index(indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .fieldNumber(HealthcareServicesDelivery.TIME_PERIODS)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.TIME_PERIOD_QUALIFIER)
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .index(indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .fieldNumber(HealthcareServicesDelivery.TIME_PERIOD_QUALIFIER)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .TIME_PERIOD_QUALIFIER_SYSTEM)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.DELIVERY_FREQUENCY)
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .index(indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .fieldNumber(HealthcareServicesDelivery.DELIVERY_FREQUENCY)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .DELIVERY_FREQUENCY_SYSTEM)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.DELIVERY_PATTERN)
                        .required(REQUIRED)
                        .filemanFactory(
                            factoryRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .index(indexRegistry().get(HealthcareServicesDelivery.FILE_NUMBER))
                        .fieldNumber(HealthcareServicesDelivery.DELIVERY_PATTERN)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions.DELIVERY_PATTERN_SYSTEM)
                        .build()))
            .build());
  }

  WriteableFilemanValue money(Money allowed, Money used) {
    if (allBlank(allowed, used)) {
      throw ExactlyOneOfFields.builder()
          .jsonPath(".insurance[].item[].benefit")
          .exactlyOneOfFields(List.of(".allowedMoney", ".usedMoney"))
          .providedFields(List.of())
          .build();
    }
    if (!isBlank(allowed) && !isBlank(used)) {
      throw ExactlyOneOfFields.builder()
          .jsonPath(".insurance[].item[].benefit")
          .exactlyOneOfFields(List.of(".allowedMoney", ".usedMoney"))
          .providedFields(List.of(".allowedMoney", ".usedMoney"))
          .build();
    }
    var money = isBlank(allowed) ? used : allowed;
    return factoryRegistry()
        .get(EligibilityBenefit.FILE_NUMBER)
        .forRequiredString(
            EligibilityBenefit.MONETARY_AMOUNT,
            indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
            money.value().toPlainString());
  }

  void outcome() {
    if (!Outcome.complete.equals(coverageEligibilityResponse().outcome())) {
      throw UnexpectedValueForField.builder()
          .valueReceived(coverageEligibilityResponse().outcome())
          .supportedValues(List.of(Outcome.complete))
          .jsonPath(".outcome")
          .build();
    }
  }

  List<WriteableFilemanValue> procedureCoding(CodeableConcept productOrService) {
    if (isBlank(productOrService) || isBlank(productOrService.coding())) {
      throw MissingRequiredField.builder()
          .jsonPath("insurance[].item[].productOrService.coding")
          .build();
    }
    if (productOrService.coding().size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath("insurance[].item[].productOrService.coding[]")
          .expectedCount(1)
          .receivedCount(productOrService.coding().size())
          .build();
    }
    Coding productOrServiceCoding = productOrService.coding().get(0);
    return Stream.of(
            factoryRegistry()
                .get(EligibilityBenefit.FILE_NUMBER)
                .forRequiredString(
                    EligibilityBenefit.PROCEDURE_CODE,
                    indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
                    productOrServiceCoding.code()),
            factoryRegistry()
                .get(EligibilityBenefit.FILE_NUMBER)
                .forRequiredString(
                    EligibilityBenefit.PROCEDURE_CODING_METHOD,
                    indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
                    productOrServiceCoding.system()))
        .toList();
  }

  WriteableFilemanValue procedureModifier(List<CodeableConcept> modifier) {
    if (isBlank(modifier)) {
      throw MissingRequiredField.builder().jsonPath(".insurance[].item[].modifier[]").build();
    }
    if (modifier.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .receivedCount(modifier.size())
          .expectedCount(1)
          .jsonPath(".insurance[].item[].modifier[]")
          .build();
    }
    return factoryRegistry()
        .get(EligibilityBenefit.FILE_NUMBER)
        .forRequiredCodeableConcept(
            EligibilityBenefit.PROCEDURE_MODIFIER_1,
            indexRegistry().get(EligibilityBenefit.FILE_NUMBER),
            modifier.get(0),
            CoverageEligibilityResponseStructureDefinitions.ITEM_MODIFIER);
  }

  void purpose() {
    if (Safe.list(coverageEligibilityResponse().purpose()).size() != 1
        || !Purpose.benefits.equals(coverageEligibilityResponse().purpose().get(0))) {
      throw UnexpectedValueForField.builder()
          .valueReceived(coverageEligibilityResponse().purpose())
          .supportedValues(List.of(Purpose.benefits))
          .jsonPath(".purpose")
          .build();
    }
  }

  void status() {
    if (!Status.active.equals(coverageEligibilityResponse().status())) {
      throw UnexpectedValueForField.builder()
          .valueReceived(coverageEligibilityResponse().status())
          .supportedValues(List.of(Status.active))
          .jsonPath(".status")
          .build();
    }
  }

  /** Transform Fhir fields into a list of fileman values. */
  public Set<WriteableFilemanValue> toVistaFiles() {
    status();
    purpose();
    outcome();
    Set<WriteableFilemanValue> vistaFields = new HashSet<>();
    vistaFields.addAll(
        coverageEligibilityResponse().identifier().stream().map(this::identifier).toList());
    vistaFields.add(
        recordCoordinatesForReference(coverageEligibilityResponse().insurer())
            .map(this::ienForPayerFileOrDie)
            .map(
                p ->
                    factoryRegistry()
                        .get(IivResponse.FILE_NUMBER)
                        .forRequiredPointerWithGraveMarker(
                            IivResponse.PAYER, indexRegistry().get(IivResponse.FILE_NUMBER), p))
            .orElseThrow(
                () ->
                    InvalidReferenceId.builder()
                        .jsonPath(".insurer")
                        .referenceType("Organization")
                        .build()));
    vistaFields.add(
        Optional.ofNullable(coverageEligibilityResponse().servicedDate())
            .map(FhirDateTime::parseDateTime)
            .map(i -> DateTimeFormatter.ofPattern("MM-dd-yyy").withZone(zoneId()).format(i))
            .map(
                factoryRegistry()
                    .get(IivResponse.FILE_NUMBER)
                    .toString(
                        IivResponse.SERVICE_DATE,
                        index(indexRegistry().get(IivResponse.FILE_NUMBER)),
                        identity()))
            .orElseThrow(
                () ->
                    UnexpectedValueForField.builder()
                        .valueReceived(coverageEligibilityResponse().servicedDate())
                        .dataType("https://www.hl7.org/fhir/datatypes.html#date")
                        .jsonPath(".servicedDate")
                        .build()));
    if (coverageEligibilityResponse().insurance() == null
        || coverageEligibilityResponse().insurance().size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".insurance[]")
          .expectedCount(1)
          .receivedCount(coverageEligibilityResponse().insurance().size())
          .build();
    }
    coverageEligibilityResponse().insurance().stream()
        .flatMap(this::insurance)
        .forEach(vistaFields::add);
    vistaFields.addAll(extensionProcessor().process(coverageEligibilityResponse().extension()));
    vistaFields.add(
        factoryRegistry()
            .get(HealthCareCodeInformation.FILE_NUMBER)
            .forRequiredInteger(
                HealthCareCodeInformation.SEQUENCE,
                indexRegistry().get(HealthCareCodeInformation.FILE_NUMBER),
                indexRegistry().get(HealthCareCodeInformation.FILE_NUMBER)));
    vistaFields.add(
        factoryRegistry()
            .get(HealthCareCodeInformation.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                indexRegistry().getAndIncrement(HealthCareCodeInformation.FILE_NUMBER),
                IivResponse.FILE_NUMBER,
                indexRegistry().get(IivResponse.FILE_NUMBER)));
    return vistaFields;
  }

  String x12YesNo(Boolean isYes) {
    if (isYes == null) {
      return "U";
    } else if (isYes) {
      return "Y";
    } else {
      return "N";
    }
  }
}
