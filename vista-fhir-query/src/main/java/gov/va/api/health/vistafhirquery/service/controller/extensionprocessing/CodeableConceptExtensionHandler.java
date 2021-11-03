package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class CodeableConceptExtensionHandler extends AbstractSingleFieldExtensionHandler {

  @Getter private final String codingSystem;

  /** All args constructor. */
  @Builder
  public CodeableConceptExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String fieldNumber,
      int index,
      @NonNull String codingSystem) {
    super(definingUrl, required, filemanFactory, fieldNumber, index);
    this.codingSystem = codingSystem;
  }

  public static CodeableConceptExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return CodeableConceptExtensionHandler.builder().definingUrl(definingUrl);
  }

  private String findCodeOrDie(String extensionUrl, List<Coding> codings) {
    if (isBlank(codings)) {
      throw BadExtension.because(extensionUrl, ".valueCodeableConcept.coding is null or empty");
    }
    var matchingCodings = codings.stream().filter(c -> codingSystem().equals(c.system())).toList();
    if (matchingCodings.size() != 1) {
      throw BadExtension.because(
          extensionUrl,
          "Found "
              + matchingCodings.size()
              + " matching .valueCodeableConcept.coding's for system "
              + codingSystem());
    }
    var coding = matchingCodings.get(0);
    if (isBlank(coding.code())) {
      throw BadExtension.because(extensionUrl, ".valueCodeableConcept.coding.code is null");
    }
    return coding.code();
  }

  @Override
  public List<WriteableFilemanValue> handle(Extension extension) {
    if (isBlank(extension.valueCodeableConcept())) {
      throw BadExtension.because(extension.url(), ".valueCodeableConcept is null");
    }
    var code = findCodeOrDie(extension.url(), extension.valueCodeableConcept().coding());
    return List.of(filemanFactory().forString(fieldNumber(), index(), code));
  }
}
