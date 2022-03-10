package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.tryFormatDate;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.tryParseDate;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForExtensionField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableDateDefinition;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableExtensionDefinition;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class DateExtensionHandler extends AbstractSingleFieldExtensionHandler {

  @Getter DateTimeFormatter dateFormatter;

  @Builder
  DateExtensionHandler(
      MappableExtensionDefinition<MappableDateDefinition> definition,
      @NonNull WriteableFilemanValueFactory filemanFactory,
      int index) {
    super(
        definition.structureDefinition(),
        definition.valueDefinition().isRequired() ? Required.REQUIRED : Required.OPTIONAL,
        filemanFactory,
        definition.valueDefinition().vistaField(),
        index);
    this.dateFormatter = definition.valueDefinition().dateFormatter();
  }

  @Override
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    if (isBlank(extension.valueDate())) {
      throw RequestPayloadExceptions.ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueDate")
          .build();
    }
    String vistaDate =
        tryParseDate(extension.valueDate())
            .flatMap(dateTime -> tryFormatDate(dateTime, dateFormatter()))
            .orElseThrow(
                () ->
                    UnexpectedValueForExtensionField.builder()
                        .jsonPath(jsonPath)
                        .definingUrl(definingUrl())
                        .dataType("http://hl7.org/fhir/R4/datatypes.html#date")
                        .valueReceived(extension.valueDate())
                        .build());
    return List.of(filemanFactory().forString(fieldNumber(), index(), vistaDate).get());
  }
}
