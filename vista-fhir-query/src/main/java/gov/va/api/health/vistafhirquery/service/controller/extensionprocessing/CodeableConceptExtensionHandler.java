package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionsHandlers.extractCodeableConceptValueForSystem;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class CodeableConceptExtensionHandler extends AbstractExtensionHandler {

  @Getter @NonNull private final String codingSystem;

  @Builder
  public CodeableConceptExtensionHandler(
      WriteableFilemanValueFactory filemanFactory,
      String definingUrl,
      IsRequired required,
      String fieldNumber,
      String codingSystem) {
    super(definingUrl, required, fieldNumber, filemanFactory);
    this.codingSystem = codingSystem;
  }

  public static CodeableConceptExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return CodeableConceptExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue handle(Extension extension) {
    var value =
        extractCodeableConceptValueForSystem(
            extension.valueCodeableConcept(), definingUrl(), codingSystem());
    if (isBlank(value)) {
      return null;
    }
    return filemanFactory().forString(fieldNumber(), 1, value);
  }
}
