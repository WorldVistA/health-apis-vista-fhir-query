package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidExtension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidNestedExtension;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
public class ComplexExtensionHandler implements ExtensionHandler {
  @Getter private final String definingUrl;

  @Getter private final Required required;

  @Getter private final List<ExtensionHandler> childExtensions;

  public static ComplexExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return ComplexExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    if (isBlank(extension.extension())) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".extension[]")
          .build();
    }
    try {
      return R4ExtensionProcessor.of(".extension[]", childExtensions())
          .process(extension.extension());
    } catch (InvalidExtension e) {
      throw InvalidNestedExtension.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .nestedProblem(e.getMessage())
          .build();
    }
  }
}
