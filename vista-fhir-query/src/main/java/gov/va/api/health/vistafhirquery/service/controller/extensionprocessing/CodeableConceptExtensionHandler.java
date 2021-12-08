package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
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

  private Stream<String> findCodeForSystem(
      String extensionUrl, List<Coding> codings, String codingSystem) {
    if (isBlank(codings)) {
      throw BadExtension.because(extensionUrl, ".valueCodeableConcept.coding is null or empty");
    }
    var matchingCodings = codings.stream().filter(c -> codingSystem.equals(c.system())).toList();
    if (matchingCodings.size() > 1) {
      throw BadExtension.because(
          extensionUrl,
          "Found "
              + matchingCodings.size()
              + " matching .valueCodeableConcept.coding's for system "
              + codingSystem);
    }
    return matchingCodings.stream().map(Coding::code);
  }

  @Override
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    if (isBlank(extension.valueCodeableConcept())) {
      throw RequestPayloadExceptions.ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueCodeableConcept")
          .build();
    }
    var codes =
        codingSystems().stream()
            .flatMap(
                system ->
                    findCodeForSystem(
                        extension.url(), extension.valueCodeableConcept().coding(), system))
            .filter(Objects::nonNull)
            .toList();
    if (codes.size() != 1) {
      throw BadExtension.because(
          definingUrl(),
          "Found " + codes.size() + " matching codes for systems " + codingSystems());
    }
    return List.of(filemanFactory().forRequiredString(fieldNumber(), index(), codes.get(0)));
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
