package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Money;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Benefit;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Insurance;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Item;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.CodeableConceptExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ComplexExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.PeriodExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.EligibilityBenefit;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.ServiceTypes;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberDates;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageEligibilityResponseToVistaFileTransformer {
  private static final WriteableFilemanValueFactory SUBSCRIBER_DATES_FACTORY =
      WriteableFilemanValueFactory.forFile(SubscriberDates.FILE_NUMBER);

  private static final WriteableFilemanValueFactory SERVICE_TYPES_FACTORY =
      WriteableFilemanValueFactory.forFile(ServiceTypes.FILE_NUMBER);

  private static final WriteableFilemanValueFactory ELIGIBILITY_BENEFIT_FACTORY =
      WriteableFilemanValueFactory.forFile(EligibilityBenefit.FILE_NUMBER);

  @NonNull CoverageEligibilityResponse coverageEligibilityResponse;

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
        ELIGIBILITY_BENEFIT_FACTORY.forRequiredCodeableConcept(
            EligibilityBenefit.ELIGIBILITY_BENEFIT_INFO,
            1,
            benefit.type(),
            CoverageEligibilityResponseStructureDefinitions.ELIGIBILITY_BENEFIT_INFO));
    filemanValues.add(money(benefit.allowedMoney(), benefit.usedMoney()));
    return filemanValues.stream();
  }

  private Stream<WriteableFilemanValue> insurance(Insurance insurance) {
    return insurance.item().stream().flatMap(this::item);
  }

  private Stream<WriteableFilemanValue> item(Item item) {
    Set<WriteableFilemanValue> filemanValues = new HashSet<>();
    /* ToDo Remove on successful create request.
     *      Could this just populate EligibilityBenefit field 14 (ServiceTypes)?
     *      EB 14 -> ST .01 -> X12 .01
     *      If possible, I think that would be my preference to avoid adding more files to
     *      correlate and track. */
    filemanValues.add(
        SERVICE_TYPES_FACTORY.forRequiredCodeableConcept(
            ServiceTypes.SERVICE_TYPES,
            1,
            item.category(),
            CoverageEligibilityResponseStructureDefinitions.SERVICE_TYPES));
    filemanValues.addAll(itemExtensionProcessor().process(item.extension()));
    filemanValues.add(
        ELIGIBILITY_BENEFIT_FACTORY.forRequiredCodeableConcept(
            EligibilityBenefit.COVERAGE_LEVEL,
            1,
            item.unit(),
            CoverageEligibilityResponseStructureDefinitions.ITEM_UNIT));
    filemanValues.add(
        ELIGIBILITY_BENEFIT_FACTORY.forRequiredCodeableConcept(
            EligibilityBenefit.TIME_PERIOD_QUALIFIER,
            1,
            item.term(),
            CoverageEligibilityResponseStructureDefinitions.ITEM_TERM));
    item.benefit().stream().flatMap(this::benefit).forEach(filemanValues::add);
    filemanValues.addAll(procedureCoding(item.productOrService()));
    filemanValues.add(procedureModifier(item.modifier()));
    filemanValues.add(
        ELIGIBILITY_BENEFIT_FACTORY.forRequiredBoolean(
            EligibilityBenefit.AUTHORIZATION_CERTIFICATION,
            1,
            item.authorizationRequired(),
            this::x12YesNo));
    filemanValues.add(
        ELIGIBILITY_BENEFIT_FACTORY.forRequiredCodeableConcept(
            EligibilityBenefit.IN_PLAN,
            1,
            item.network(),
            CoverageEligibilityResponseStructureDefinitions.X12_YES_NO_SYSTEM));
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
                        .filemanFactory(SUBSCRIBER_DATES_FACTORY)
                        .zoneId(zoneId())
                        .periodStartFieldNumber(SubscriberDates.DATE)
                        .periodEndFieldNumber(SubscriberDates.DATE)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE_KIND)
                        .required(REQUIRED)
                        .filemanFactory(SUBSCRIBER_DATES_FACTORY)
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_DATE_QUALIFIER)
                        .fieldNumber(SubscriberDates.DATE_QUALIFIER)
                        .build()))
            .build());
  }

  private WriteableFilemanValue money(Money allowed, Money used) {
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
    return ELIGIBILITY_BENEFIT_FACTORY.forRequiredString(
        EligibilityBenefit.MONETARY_AMOUNT, 1, money.value().toPlainString());
  }

  private List<WriteableFilemanValue> procedureCoding(CodeableConcept productOrService) {
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
            ELIGIBILITY_BENEFIT_FACTORY.forRequiredString(
                EligibilityBenefit.PROCEDURE_CODE, 1, productOrServiceCoding.code()),
            ELIGIBILITY_BENEFIT_FACTORY.forRequiredString(
                EligibilityBenefit.PROCEDURE_CODING_METHOD, 1, productOrServiceCoding.system()))
        .toList();
  }

  private WriteableFilemanValue procedureModifier(List<CodeableConcept> modifier) {
    if (isBlank(modifier)) {
      throw BadRequestPayload.because(
          EligibilityBenefit.PROCEDURE_MODIFIER_1, "Required item modifier is missing");
    }
    if (modifier.size() != 1) {
      throw BadRequestPayload.because(
          EligibilityBenefit.PROCEDURE_MODIFIER_1, "More than one item modifier provided");
    }
    return ELIGIBILITY_BENEFIT_FACTORY.forRequiredCodeableConcept(
        EligibilityBenefit.PROCEDURE_MODIFIER_1,
        1,
        modifier.get(0),
        CoverageEligibilityResponseStructureDefinitions.ITEM_MODIFIER);
  }

  /** Transform Fhir fields into a list of fileman values. */
  public Set<WriteableFilemanValue> toVistaFiles() {
    Set<WriteableFilemanValue> vistaFields = new HashSet<>();
    coverageEligibilityResponse().insurance().stream()
        .flatMap(this::insurance)
        .forEach(vistaFields::add);
    return vistaFields;
  }

  private String x12YesNo(Boolean isYes) {
    if (isYes == null) {
      return "U";
    } else if (isYes) {
      return "Y";
    }
    return "N";
  }
}
