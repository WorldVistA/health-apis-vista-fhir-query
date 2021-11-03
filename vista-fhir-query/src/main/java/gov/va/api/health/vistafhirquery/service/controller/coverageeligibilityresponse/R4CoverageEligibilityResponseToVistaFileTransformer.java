package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.fhir.api.FhirDateTime.parseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.recordCoordinatesForReference;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static java.util.function.Function.identity;

import gov.va.api.health.fhir.api.FhirDateTime;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Money;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Benefit;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Insurance;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Item;
import gov.va.api.health.vistafhirquery.service.controller.FilemanFactoryRegistry;
import gov.va.api.health.vistafhirquery.service.controller.FilemanIndexRegistry;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.CodeableConceptExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ComplexExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.PeriodExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.StringExtensionHandler;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.EligibilityBenefit;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.IivResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.ServiceTypes;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberDates;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
  private static final FilemanFactoryRegistry FACTORY_REGISTRY = FilemanFactoryRegistry.create();

  private static final FilemanIndexRegistry INDEX_REGISTRY = FilemanIndexRegistry.create();

  CoverageEligibilityResponse coverageEligibilityResponse;

  ZoneId zoneId;

  @Builder
  R4CoverageEligibilityResponseToVistaFileTransformer(
      @NonNull CoverageEligibilityResponse coverageEligibilityResponse, ZoneId timezone) {
    this.coverageEligibilityResponse = coverageEligibilityResponse;
    this.zoneId = timezone == null ? ZoneId.of("UTC") : timezone;
  }

  private Stream<WriteableFilemanValue> benefit(Benefit benefit) {
    Set<WriteableFilemanValue> filemanValues = new HashSet<>();
    filemanValues.add(
        FACTORY_REGISTRY
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredCodeableConcept(
                EligibilityBenefit.ELIGIBILITY_BENEFIT_INFO,
                INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
                benefit.type(),
                CoverageEligibilityResponseStructureDefinitions.ELIGIBILITY_BENEFIT_INFO));
    filemanValues.add(money(benefit.allowedMoney(), benefit.usedMoney()));
    return filemanValues.stream();
  }

  WriteableFilemanValue dateTimePeriod(Period period) {
    if (isBlank(period)) {
      throw BadRequestPayload.because(
          IivResponse.FILE_NUMBER,
          IivResponse.DATE_TIME_PERIOD,
          ".benefitPeriod is a required field.");
    }
    if (isBlank(period.start())) {
      throw BadRequestPayload.because(
          IivResponse.FILE_NUMBER,
          IivResponse.DATE_TIME_PERIOD,
          ".benefitPeriod requires a start date.");
    }
    var formatter = DateTimeFormatter.ofPattern("MMddyyyy").withZone(zoneId());
    var vistaDateTimePeriod = formatter.format(parseDateTime(period.start()));
    if (!isBlank(period.end())) {
      var formattedEndDate = formatter.format(parseDateTime(period.end()));
      vistaDateTimePeriod = vistaDateTimePeriod + "-" + formattedEndDate;
    }
    return FACTORY_REGISTRY
        .get(IivResponse.FILE_NUMBER)
        .forRequiredString(
            IivResponse.DATE_TIME_PERIOD,
            INDEX_REGISTRY.get(IivResponse.FILE_NUMBER),
            vistaDateTimePeriod);
  }

  private R4ExtensionProcessor extensionProcessor() {
    return R4ExtensionProcessor.of(
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_INFO_STATUS_CODE_DEFINITION)
            .filemanFactory(FACTORY_REGISTRY.get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_INFO_STATUS_CODE)
            .codingSystem(CoverageEligibilityResponseStructureDefinitions.MILITARY_INFO_STATUS_CODE)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_EMPLOYMENT_STATUS_DEFINITION)
            .filemanFactory(FACTORY_REGISTRY.get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_EMPLOYMENT_STATUS)
            .codingSystem(
                CoverageEligibilityResponseStructureDefinitions.MILITARY_EMPLOYMENT_STATUS)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_GOVT_AFFILIATION_CODE_DEFINITION)
            .filemanFactory(FACTORY_REGISTRY.get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_GOVT_AFFILIATION_CODE)
            .codingSystem(
                CoverageEligibilityResponseStructureDefinitions.MILITARY_GOVT_AFFILIATION_CODE)
            .required(REQUIRED)
            .build(),
        StringExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_PERSONNEL_DESCRIPTION_DEFINITION)
            .filemanFactory(FACTORY_REGISTRY.get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_PERSONNEL_DESCRIPTION)
            .required(REQUIRED)
            .build(),
        CodeableConceptExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions
                    .MILITARY_SERVICE_RANK_CODE_DEFINITION)
            .filemanFactory(FACTORY_REGISTRY.get(IivResponse.FILE_NUMBER))
            .fieldNumber(IivResponse.MILITARY_SERVICE_RANK_CODE)
            .codingSystem(
                CoverageEligibilityResponseStructureDefinitions.MILITARY_SERVICE_RANK_CODE)
            .required(REQUIRED)
            .build());
  }

  WriteableFilemanValue identifier(@NonNull Identifier identifier) {
    if (isBlank(identifier.type()) || isBlank(identifier.type().text())) {
      throw new BadRequestPayload("Unknown Identifier: " + identifier);
    }
    switch (identifier.type().text()) {
      case "MSH-10":
        return FACTORY_REGISTRY
            .get(IivResponse.FILE_NUMBER)
            .forRequiredIdentifier(
                IivResponse.MESSAGE_CONTROL_ID,
                INDEX_REGISTRY.get(IivResponse.FILE_NUMBER),
                identifier);
      case "MSA-3":
        return FACTORY_REGISTRY
            .get(IivResponse.FILE_NUMBER)
            .forRequiredIdentifier(
                IivResponse.TRACE_NUMBER, INDEX_REGISTRY.get(IivResponse.FILE_NUMBER), identifier);
      default:
        throw new IllegalArgumentException("Unknown Identifier type: " + identifier.type().text());
    }
  }

  private String ienForPayerFileOrDie(RecordCoordinates coordinates) {
    if (!Payer.FILE_NUMBER.equals(coordinates.file())) {
      throw BadRequestPayload.because(
          IivResponse.FILE_NUMBER, IivResponse.PAYER, ".insurer reference was not to a payer.");
    }
    return coordinates.ien();
  }

  private Stream<WriteableFilemanValue> insurance(Insurance insurance) {
    Set<WriteableFilemanValue> filemanValues = new HashSet<>();
    insurance.item().stream().flatMap(this::item).forEach(filemanValues::add);
    filemanValues.add(dateTimePeriod(insurance.benefitPeriod()));
    return filemanValues.stream();
  }

  private Stream<WriteableFilemanValue> item(Item item) {
    Set<WriteableFilemanValue> filemanValues = new HashSet<>();
    filemanValues.add(
        FACTORY_REGISTRY
            .get(ServiceTypes.FILE_NUMBER)
            .forRequiredCodeableConcept(
                ServiceTypes.SERVICE_TYPES,
                INDEX_REGISTRY.get(ServiceTypes.FILE_NUMBER),
                item.category(),
                CoverageEligibilityResponseStructureDefinitions.SERVICE_TYPES));
    filemanValues.add(
        FACTORY_REGISTRY
            .get(ServiceTypes.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                INDEX_REGISTRY.getAndIncrement(ServiceTypes.FILE_NUMBER),
                EligibilityBenefit.FILE_NUMBER,
                INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER)));
    filemanValues.addAll(itemExtensionProcessor().process(item.extension()));
    filemanValues.add(
        FACTORY_REGISTRY
            .get(SubscriberDates.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                INDEX_REGISTRY.getAndIncrement(SubscriberDates.FILE_NUMBER),
                EligibilityBenefit.FILE_NUMBER,
                INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER)));
    filemanValues.add(
        FACTORY_REGISTRY
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredCodeableConcept(
                EligibilityBenefit.COVERAGE_LEVEL,
                INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
                item.unit(),
                CoverageEligibilityResponseStructureDefinitions.ITEM_UNIT));
    filemanValues.add(
        FACTORY_REGISTRY
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredCodeableConcept(
                EligibilityBenefit.TIME_PERIOD_QUALIFIER,
                INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
                item.term(),
                CoverageEligibilityResponseStructureDefinitions.ITEM_TERM));
    item.benefit().stream().flatMap(this::benefit).forEach(filemanValues::add);
    filemanValues.addAll(procedureCoding(item.productOrService()));
    filemanValues.add(procedureModifier(item.modifier()));
    filemanValues.add(
        FACTORY_REGISTRY
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredBoolean(
                EligibilityBenefit.AUTHORIZATION_CERTIFICATION,
                INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
                item.authorizationRequired(),
                this::x12YesNo));
    filemanValues.add(
        FACTORY_REGISTRY
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredCodeableConcept(
                EligibilityBenefit.IN_PLAN,
                INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
                item.network(),
                CoverageEligibilityResponseStructureDefinitions.X12_YES_NO_SYSTEM));
    filemanValues.add(
        FACTORY_REGISTRY
            .get(EligibilityBenefit.FILE_NUMBER)
            .forRequiredParentFileUsingIenMacro(
                INDEX_REGISTRY.getAndIncrement(EligibilityBenefit.FILE_NUMBER),
                IivResponse.FILE_NUMBER,
                INDEX_REGISTRY.get(IivResponse.FILE_NUMBER)));
    return filemanValues.stream();
  }

  private R4ExtensionProcessor itemExtensionProcessor() {
    return R4ExtensionProcessor.of(
        ComplexExtensionHandler.forDefiningUrl(
                CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE)
            .required(REQUIRED)
            .childExtensions(
                List.of(
                    PeriodExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE_PERIOD)
                        .required(REQUIRED)
                        .filemanFactory(FACTORY_REGISTRY.get(SubscriberDates.FILE_NUMBER))
                        .zoneId(zoneId())
                        .periodStartFieldNumber(SubscriberDates.DATE)
                        .periodEndFieldNumber(SubscriberDates.DATE)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE_KIND)
                        .required(REQUIRED)
                        .filemanFactory(FACTORY_REGISTRY.get(SubscriberDates.FILE_NUMBER))
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_DATE_QUALIFIER)
                        .fieldNumber(SubscriberDates.DATE_QUALIFIER)
                        .build()))
            .build());
  }

  WriteableFilemanValue money(Money allowed, Money used) {
    if (allBlank(allowed, used)) {
      throw BadRequestPayload.because(
          EligibilityBenefit.FILE_NUMBER,
          EligibilityBenefit.MONETARY_AMOUNT,
          "Expected one of allowedMoney or usedMoney, but got neither.");
    }
    if (!isBlank(allowed) && !isBlank(used)) {
      throw BadRequestPayload.because(
          EligibilityBenefit.FILE_NUMBER,
          EligibilityBenefit.MONETARY_AMOUNT,
          "Expected one of allowedMoney or usedMoney, but got both.");
    }
    var money = isBlank(allowed) ? used : allowed;
    return FACTORY_REGISTRY
        .get(EligibilityBenefit.FILE_NUMBER)
        .forRequiredString(
            EligibilityBenefit.MONETARY_AMOUNT,
            INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
            money.value().toPlainString());
  }

  List<WriteableFilemanValue> procedureCoding(CodeableConcept productOrService) {
    if (isBlank(productOrService) || isBlank(productOrService.coding())) {
      throw BadRequestPayload.because(
          EligibilityBenefit.FILE_NUMBER,
          EligibilityBenefit.PROCEDURE_CODING_METHOD,
          "Required productOrService.coding is missing");
    }
    if (productOrService.coding().size() != 1) {
      throw BadRequestPayload.because(
          EligibilityBenefit.FILE_NUMBER,
          EligibilityBenefit.PROCEDURE_CODING_METHOD,
          "productOrService expected 1 coding but got " + productOrService.coding().size());
    }
    Coding productOrServiceCoding = productOrService.coding().get(0);
    return Stream.of(
            FACTORY_REGISTRY
                .get(EligibilityBenefit.FILE_NUMBER)
                .forRequiredString(
                    EligibilityBenefit.PROCEDURE_CODE,
                    INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
                    productOrServiceCoding.code()),
            FACTORY_REGISTRY
                .get(EligibilityBenefit.FILE_NUMBER)
                .forRequiredString(
                    EligibilityBenefit.PROCEDURE_CODING_METHOD,
                    INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
                    productOrServiceCoding.system()))
        .toList();
  }

  WriteableFilemanValue procedureModifier(List<CodeableConcept> modifier) {
    if (isBlank(modifier)) {
      throw BadRequestPayload.because(
          EligibilityBenefit.PROCEDURE_MODIFIER_1, "Required item modifier is missing");
    }
    if (modifier.size() != 1) {
      throw BadRequestPayload.because(
          EligibilityBenefit.PROCEDURE_MODIFIER_1, "More than one item modifier provided");
    }
    return FACTORY_REGISTRY
        .get(EligibilityBenefit.FILE_NUMBER)
        .forRequiredCodeableConcept(
            EligibilityBenefit.PROCEDURE_MODIFIER_1,
            INDEX_REGISTRY.get(EligibilityBenefit.FILE_NUMBER),
            modifier.get(0),
            CoverageEligibilityResponseStructureDefinitions.ITEM_MODIFIER);
  }

  /** Transform Fhir fields into a list of fileman values. */
  public Set<WriteableFilemanValue> toVistaFiles() {
    Set<WriteableFilemanValue> vistaFields = new HashSet<>();
    vistaFields.addAll(
        coverageEligibilityResponse().identifier().stream().map(this::identifier).toList());
    vistaFields.add(
        recordCoordinatesForReference(coverageEligibilityResponse().insurer())
            .map(
                FACTORY_REGISTRY
                    .get(IivResponse.FILE_NUMBER)
                    .toString(
                        IivResponse.PAYER,
                        index(INDEX_REGISTRY.get(IivResponse.FILE_NUMBER)),
                        this::ienForPayerFileOrDie))
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        IivResponse.FILE_NUMBER,
                        IivResponse.PAYER,
                        "The .insurer field was not a valid reference.")));
    vistaFields.add(
        Optional.ofNullable(coverageEligibilityResponse().servicedDate())
            .map(FhirDateTime::parseDateTime)
            .map(i -> DateTimeFormatter.ofPattern("MM-dd-yyy").withZone(zoneId()).format(i))
            .map(
                FACTORY_REGISTRY
                    .get(IivResponse.FILE_NUMBER)
                    .toString(
                        IivResponse.SERVICE_DATE,
                        index(INDEX_REGISTRY.get(IivResponse.FILE_NUMBER)),
                        identity()))
            .orElseThrow(
                () ->
                    BadRequestPayload.because(
                        IivResponse.FILE_NUMBER,
                        IivResponse.SERVICE_DATE,
                        "The .servicedDate field was not a valid date.")));
    coverageEligibilityResponse().insurance().stream()
        .flatMap(this::insurance)
        .forEach(vistaFields::add);
    vistaFields.addAll(extensionProcessor().process(coverageEligibilityResponse().extension()));
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
