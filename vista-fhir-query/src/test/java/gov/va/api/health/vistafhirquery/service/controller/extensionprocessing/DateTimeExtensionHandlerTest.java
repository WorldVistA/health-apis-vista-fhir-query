package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class DateTimeExtensionHandlerTest {
  static Stream<Arguments> handleDateTime() {
    return Stream.of(
        arguments(
            "2010-01-20T10:00:01.000Z",
            List.of(
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(1)
                    .field(".dateTime")
                    .value("01-20-2010")
                    .build())),
        arguments(
            "2015-02-08",
            List.of(
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(1)
                    .field(".dateTime")
                    .value("02-08-2015")
                    .build())));
  }

  private DateTimeExtensionHandler _handler(int index) {
    return DateTimeExtensionHandler.forDefiningUrl("http://fugazi.com/dateTime")
        .required(Required.REQUIRED)
        .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
        .dateTimeFormatter(DateTimeFormatter.ofPattern("MM-dd-yyy").withZone(ZoneId.of("UTC")))
        .dateTimeFieldNumber(".dateTime")
        .index(index)
        .build();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void blankExtensionDateTime(String dateTime) {
    var sample = extensionWithDateTime(dateTime);
    assertThatExceptionOfType(ExtensionMissingRequiredField.class)
        .isThrownBy(() -> _handler(1).handle(".fugazi", sample));
  }

  private Extension extensionWithDateTime(String dateTime) {
    return Extension.builder().valueDateTime(dateTime).build();
  }

  @ParameterizedTest
  @MethodSource
  void handleDateTime(String dateTime, List<WriteableFilemanValue> expected) {
    var sample = extensionWithDateTime(dateTime);
    assertThat(_handler(1).handle(".fugazi", sample)).containsExactlyElementsOf(expected);
  }
}
