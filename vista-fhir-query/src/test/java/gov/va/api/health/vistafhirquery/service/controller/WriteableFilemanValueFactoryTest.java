package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class WriteableFilemanValueFactoryTest {
  private final WriteableFilemanValueFactory factory =
      WriteableFilemanValueFactory.forFile("fugazi");

  @Test
  void autoincrement() {
    var index1 = WriteableFilemanValueFactory.autoincrement();
    var index2 = WriteableFilemanValueFactory.autoincrement();
    assertThat(index1.get()).isEqualTo(1);
    assertThat(index1.get()).isEqualTo(2);
    assertThat(index1.get()).isEqualTo(3);
    // indexes do not interact
    assertThat(index2.get()).isEqualTo(1);
  }

  @Test
  void extensionToInteger() {
    assertThat(factory.extensionToInteger("x", () -> 3).apply(null)).isNull();
    assertThat(factory.extensionToInteger("x", () -> 3).apply(Extension.builder().build()))
        .isNull();
    assertThat(
            factory
                .extensionToInteger("x", () -> 3)
                .apply(Extension.builder().valueInteger(8).build()))
        .isEqualTo(writeableFilemanValue("x", 3, "8"));
  }

  @Test
  void forBooleanExtension() {
    Function<Boolean, String> boolToString = value -> value ? "YES" : "NO";
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> factory.forRequiredBoolean("x", 1, (Extension) null, boolToString));
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () -> factory.forRequiredBoolean("x", 1, Extension.builder().build(), boolToString));
    var extension = Extension.builder().build();
    assertThat(factory.forRequiredBoolean("x", 1, extension.valueBoolean(true), boolToString))
        .isEqualTo(writeableFilemanValue("x", 1, "YES"));
    assertThat(factory.forRequiredBoolean("x", 1, extension.valueBoolean(false), boolToString))
        .isEqualTo(writeableFilemanValue("x", 1, "NO"));
  }

  @Test
  void forIntegerExtension() {
    assertThat(factory.forOptionalInteger("x", 1, (Extension) null)).isEmpty();
    assertThat(factory.forOptionalInteger("x", 1, Extension.builder().build())).isEmpty();
    assertThat(factory.forOptionalInteger("x", 1, Extension.builder().valueInteger(8).build()))
        .contains(writeableFilemanValue("x", 1, "8"));
  }

  @Test
  void forOptionalInteger() {
    assertThat(factory.forOptionalInteger("x", 1, (Integer) null)).isEmpty();
    assertThat(factory.forOptionalInteger("x", 1, 8)).contains(writeableFilemanValue("x", 1, "8"));
  }

  @Test
  void forOptionalPointer() {
    assertThat(factory.forOptionalPointer("fugazi", 1, null)).isEmpty();
    assertThat(factory.forOptionalPointer("fugazi", 1, " ")).isEmpty();
    assertThat(factory.forOptionalPointer("fugazi", 1, "8"))
        .contains(writeableFilemanValue("ien", 1, "8"));
  }

  @Test
  void forOptionalString() {
    assertThat(factory.forOptionalString("x", 1, null)).isEmpty();
    assertThat(factory.forOptionalString("x", 1, " ")).isEmpty();
    assertThat(factory.forOptionalString("x", 1, "shanktopus"))
        .contains(writeableFilemanValue("x", 1, "shanktopus"));
  }

  @Test
  void forRequiredBoolean() {
    Function<Boolean, String> boolToString = value -> value ? "YES" : "NO";
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> factory.forRequiredBoolean("x", 1, (Boolean) null, boolToString));
    assertThat(factory.forRequiredBoolean("x", 1, true, boolToString))
        .isEqualTo(writeableFilemanValue("x", 1, "YES"));
    assertThat(factory.forRequiredBoolean("x", 1, false, boolToString))
        .isEqualTo(writeableFilemanValue("x", 1, "NO"));
  }

  @Test
  void forRequiredIdentifier() {
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> factory.forRequiredIdentifier("x", 1, null));
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> factory.forRequiredIdentifier("x", 1, Identifier.builder().build()));
    assertThat(
            factory.forRequiredIdentifier("x", 1, Identifier.builder().value("shanktopus").build()))
        .isEqualTo(writeableFilemanValue("x", 1, "shanktopus"));
  }

  @Test
  void forRequiredInteger() {
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> factory.forRequiredInteger("x", 1, (Integer) null));
    assertThat(factory.forRequiredInteger("x", 1, 8)).isEqualTo(writeableFilemanValue("x", 1, "8"));
  }

  @Test
  void forRequiredString() {
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> factory.forRequiredString("x", 1, null));
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> factory.forRequiredString("x", 1, " "));
    assertThat(factory.forRequiredString("x", 1, "shanktopus"))
        .isEqualTo(writeableFilemanValue("x", 1, "shanktopus"));
  }

  @Test
  void index() {
    var index1 = WriteableFilemanValueFactory.index(1);
    var index2 = WriteableFilemanValueFactory.index(2);
    assertThat(index1.get()).isEqualTo(1);
    assertThat(index1.get()).isEqualTo(1);
    assertThat(index1.get()).isEqualTo(1);
    // indexes do not interact
    assertThat(index2.get()).isEqualTo(2);
  }

  @Test
  void patientTypeCoordinatesToPointer() {
    assertThat(
            factory
                .patientTypeCoordinatesToPointer("fugazi", () -> 3)
                .apply(
                    PatientTypeCoordinates.builder().site("666").icn("123V456").ien("123").build()))
        .isEqualTo(writeableFilemanValue("ien", 3, "123"));
  }

  @Test
  void recordCoordinatesToPointer() {
    assertThat(
            factory
                .recordCoordinatesToPointer("fugazi", () -> 3)
                .apply(RecordCoordinates.builder().site("666").file("fugazi").ien("123").build()))
        .isEqualTo(writeableFilemanValue("ien", 3, "123"));
  }

  @Test
  void toStringFunctional() {
    assertThat(factory.toString("x", () -> 3, s -> s + "!").apply("shanktopus"))
        .isEqualTo(writeableFilemanValue("x", 3, "shanktopus!"));
  }

  private WriteableFilemanValue writeableFilemanValue(String field, int index, String value) {
    return WriteableFilemanValue.builder()
        .file("fugazi")
        .index(index)
        .field(field)
        .value(value)
        .build();
  }
}
