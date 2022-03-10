package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableCodeDefinition;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableExtensionDefinition;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class CodeExtensionHandler extends AbstractSingleFieldExtensionHandler {
  @Getter private final Function<String, String> toCode;

  /** Constructor. */
  @Builder
  public CodeExtensionHandler(
      @NonNull MappableExtensionDefinition<MappableCodeDefinition<String, String>> definition,
      @NonNull WriteableFilemanValueFactory filemanFactory,
      int index) {
    super(
        definition.structureDefinition(),
        definition.valueDefinition().isRequired() ? Required.REQUIRED : Required.OPTIONAL,
        filemanFactory,
        definition.valueDefinition().vistaField(),
        index);
    this.toCode = definition.valueDefinition().fromCode();
  }

  @Override
  public List<LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue> handle(
      String jsonPath, @NonNull Extension extension) {
    String value = toCode.apply(extension.valueCode());
    if (isBlank(value)) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".extension[].valueCode")
          .build();
    }
    return List.of(filemanFactory().forString(fieldNumber(), index(), value).get());
  }
}
