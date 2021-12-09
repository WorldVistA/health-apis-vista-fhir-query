package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionFieldHasUnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class CodeableConceptExtensionHandler extends AbstractSingleFieldExtensionHandler {

  @Getter private final List<String> codingSystems;

  /** All args constructor. */
  @Builder
  public CodeableConceptExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String fieldNumber,
      int index,
      @NonNull List<String> codingSystems) {
    super(definingUrl, required, filemanFactory, fieldNumber, index);
    this.codingSystems = codingSystems;
  }

  public static CodeableConceptExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return CodeableConceptExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    if (isBlank(extension.valueCodeableConcept())) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueCodeableConcept")
          .build();
    }
    var matchingCodings =
        Safe.stream(extension.valueCodeableConcept().coding())
            .filter(coding -> codingSystems().contains(coding.system()))
            .toList();
    if (matchingCodings.size() != 1) {
      throw ExtensionFieldHasUnexpectedNumberOfValues.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .identifyingFieldJsonPath(".valueCodeableConcept.coding[].system")
          .identifyingFieldValue(codingSystems())
          .expectedCount(1)
          .receivedCount(matchingCodings.size())
          .build();
    }
    var code = matchingCodings.get(0).code();
    if (isBlank(code)) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueCodeableConcept.coding[].code")
          .build();
    }
    return List.of(filemanFactory().forRequiredString(fieldNumber(), index(), code));
  }

  public static class CodeableConceptExtensionHandlerBuilder {
    /** Add a single system to the codingSystems array. */
    public CodeableConceptExtensionHandlerBuilder codingSystem(String system) {
      if (codingSystems == null) {
        this.codingSystems = new ArrayList<>();
      }
      codingSystems.add(system);
      return this;
    }
  }
}
