package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.OPTIONAL;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class R4ExtensionProcessorTest {
  List<Extension> extensions() {
    ArrayList<Extension> extensions = new ArrayList<>();
    extensions.add(Extension.builder().url("TACOS").valueCode("MEXICAN").build());
    extensions.add(Extension.builder().url("FRENCH TOAST").valueCode("FRENCH").build());
    extensions.add(Extension.builder().url("SUSHI").valueCode("JAPANESE").build());
    extensions.add(Extension.builder().url("HUNGRY").valueBoolean(true).build());
    return extensions;
  }

  @Test
  void process() {
    R4ExtensionProcessor processor = processor();
    assertThat(processor.process(extensions()))
        .containsExactlyInAnyOrder(
            WriteableFilemanValue.builder()
                .file("FOODS")
                .value("true")
                .index(1)
                .field("#.ALLDAY")
                .build(),
            WriteableFilemanValue.builder()
                .file("FOODS")
                .value("MEXICAN")
                .index(1)
                .field("#.LUNCH")
                .build(),
            WriteableFilemanValue.builder()
                .file("FOODS")
                .value("JAPANESE")
                .index(1)
                .field("#.DINNER")
                .build(),
            WriteableFilemanValue.builder()
                .file("FOODS")
                .value("FRENCH")
                .index(1)
                .field("#.BREAKFAST")
                .build());
  }

  @Test
  void processThrowsBadPayloadWhenDuplicateExtensionIsFound() {
    var extensions = extensions();
    extensions.add(Extension.builder().url("SUSHI").valueCode("JAPANESE").build());
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(() -> processor().process(extensions));
  }

  @Test
  void processThrowsBadPayloadWhenExtensionHasBadDefiningUrl() {
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(() -> processor().process(Extension.builder().build().asList()));
  }

  @Test
  void processThrowsBadPayloadWhenMissingRequiredExtension() {
    var extensions = extensions();
    extensions.remove(0);
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(() -> processor().process(extensions));
  }

  @Test
  void processThrowsBadPayloadWhenNoMatchingHandlerIsFound() {
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(() -> processor().process(Extension.builder().url("DOENER").build().asList()));
  }

  private R4ExtensionProcessor processor() {
    WriteableFilemanValueFactory filemanFactory = WriteableFilemanValueFactory.forFile("FOODS");
    return R4ExtensionProcessor.of(
        FoobarExtensionHandler.builder()
            .filemanFactory(filemanFactory)
            .definingUrl("HUNGRY")
            .required(REQUIRED)
            .fieldNumber("#.ALLDAY")
            .index(1)
            .build(),
        FugaziExtensionHandler.builder()
            .filemanFactory(filemanFactory)
            .definingUrl("TACOS")
            .required(REQUIRED)
            .fieldNumber("#.LUNCH")
            .index(1)
            .build(),
        FugaziExtensionHandler.builder()
            .filemanFactory(filemanFactory)
            .definingUrl("FRENCH TOAST")
            .required(OPTIONAL)
            .fieldNumber("#.BREAKFAST")
            .index(1)
            .build(),
        FugaziExtensionHandler.builder()
            .filemanFactory(filemanFactory)
            .definingUrl("SUSHI")
            .required(REQUIRED)
            .fieldNumber("#.DINNER")
            .index(1)
            .build());
  }

  static class FugaziExtensionHandler extends AbstractExtensionHandler {

    @Getter private final String fieldNumber;

    @Builder
    protected FugaziExtensionHandler(
        String definingUrl,
        Required required,
        String fieldNumber,
        int index,
        WriteableFilemanValueFactory filemanFactory) {
      super(definingUrl, required, filemanFactory, index);
      this.fieldNumber = fieldNumber;
    }

    @Override
    public List<WriteableFilemanValue> handle(Extension extension) {
      return List.of(filemanFactory().forString(fieldNumber(), index(), extension.valueCode()));
    }
  }

  static class FoobarExtensionHandler extends AbstractSingleFieldExtensionHandler {
    @Builder
    protected FoobarExtensionHandler(
        String definingUrl,
        Required required,
        String fieldNumber,
        int index,
        WriteableFilemanValueFactory filemanFactory) {
      super(definingUrl, required, filemanFactory, fieldNumber, index);
    }

    @Override
    public List<WriteableFilemanValue> handle(Extension extension) {
      return List.of(
          filemanFactory().forString(fieldNumber(), index(), extension.valueBoolean().toString()));
    }
  }
}
