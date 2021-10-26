package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;

import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Insurance;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Item;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.CodeableConceptExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ComplexExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.PeriodExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
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
  @NonNull CoverageEligibilityResponse coverageEligibilityResponse;

  ZoneId zoneId;

  WriteableFilemanValueFactory subscriberDates =
      WriteableFilemanValueFactory.forFile(SubscriberDates.FILE_NUMBER);

  WriteableFilemanValueFactory serviceTypes =
      WriteableFilemanValueFactory.forFile(ServiceTypes.FILE_NUMBER);

  @Builder
  R4CoverageEligibilityResponseToVistaFileTransformer(
      @NonNull CoverageEligibilityResponse coverageEligibilityResponse, ZoneId timezone) {
    this.coverageEligibilityResponse = coverageEligibilityResponse;
    this.zoneId = timezone == null ? ZoneId.of("UTC") : timezone;
  }

  private Stream<WriteableFilemanValue> insurance(Insurance insurance) {
    return insurance.item().stream().flatMap(this::item);
  }

  private Stream<WriteableFilemanValue> item(Item item) {
    Set<WriteableFilemanValue> filemanValues = new HashSet<>();
    filemanValues.add(
        serviceTypes()
            .forRequiredCodeableConcept(
                ServiceTypes.SERVICE_TYPES,
                1,
                item.category(),
                CoverageEligibilityResponseStructureDefinitions.SERVICE_TYPES));
    filemanValues.addAll(itemExtensionProcessor().process(item.extension()));
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
                        .filemanFactory(subscriberDates())
                        .zoneId(zoneId())
                        .periodStartFieldNumber(SubscriberDates.DATE)
                        .periodEndFieldNumber(SubscriberDates.DATE)
                        .build(),
                    CodeableConceptExtensionHandler.forDefiningUrl(
                            CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE_KIND)
                        .required(REQUIRED)
                        .filemanFactory(subscriberDates())
                        .codingSystem(
                            CoverageEligibilityResponseStructureDefinitions
                                .SUBSCRIBER_DATE_QUALIFIER)
                        .fieldNumber(SubscriberDates.DATE_QUALIFIER)
                        .build()))
            .build());
  }

  /** Transform Fhir fields into a list of fileman values. */
  public Set<WriteableFilemanValue> toVistaFiles() {
    Set<WriteableFilemanValue> vistaFields = new HashSet<>();
    coverageEligibilityResponse().insurance().stream()
        .flatMap(this::insurance)
        .forEach(vistaFields::add);
    return vistaFields;
  }
}
