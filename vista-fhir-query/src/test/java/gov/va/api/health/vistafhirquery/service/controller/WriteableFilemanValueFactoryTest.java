package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
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
  void forBoolean() {
    assertThat(factory.forBoolean("x", 1, (Boolean) null)).isNull();
    assertThat(factory.forBoolean("x", 1, true)).isEqualTo(writeableFilemanValue("x", 1, "YES"));
    assertThat(factory.forBoolean("x", 1, false)).isEqualTo(writeableFilemanValue("x", 1, "NO"));
  }

  @Test
  void forBooleanExtension() {
    assertThat(factory.forBoolean("x", 1, (Extension) null)).isNull();
    assertThat(factory.forBoolean("x", 1, Extension.builder().build())).isNull();
    var extension = Extension.builder().build();
    assertThat(factory.forBoolean("x", 1, extension.valueBoolean(true)))
        .isEqualTo(writeableFilemanValue("x", 1, "YES"));
    assertThat(factory.forBoolean("x", 1, extension.valueBoolean(false)))
        .isEqualTo(writeableFilemanValue("x", 1, "NO"));
  }

  @Test
  void forIdentifier() {
    assertThat(factory.forIdentifier("x", 1, null)).isNull();
    assertThat(factory.forIdentifier("x", 1, Identifier.builder().build())).isNull();
    assertThat(factory.forIdentifier("x", 1, Identifier.builder().value("shanktopus").build()))
        .isEqualTo(writeableFilemanValue("x", 1, "shanktopus"));
  }

  @Test
  void forInteger() {
    assertThat(factory.forInteger("x", 1, (Integer) null)).isNull();
    assertThat(factory.forInteger("x", 1, 8)).isEqualTo(writeableFilemanValue("x", 1, "8"));
  }

  @Test
  void forIntegerExtension() {
    assertThat(factory.forInteger("x", 1, (Extension) null)).isNull();
    assertThat(factory.forInteger("x", 1, Extension.builder().build())).isNull();
    assertThat(factory.forInteger("x", 1, Extension.builder().valueInteger(8).build()))
        .isEqualTo(writeableFilemanValue("x", 1, "8"));
  }

  @Test
  void forString() {
    assertThat(factory.forString("x", 1, null)).isNull();
    assertThat(factory.forString("x", 1, " ")).isNull();
    assertThat(factory.forString("x", 1, "shanktopus"))
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
                    PatientTypeCoordinates.builder()
                        .siteId("666")
                        .icn("123V456")
                        .recordId("123")
                        .build()))
        .isEqualTo(writeableFilemanValue("ien", 3, "123"));
  }

  @Test
  void pointer() {
    assertThat(factory.forPointer("fugazi", 1, null)).isNull();
    assertThat(factory.forPointer("fugazi", 1, " ")).isNull();
    assertThat(factory.forPointer("fugazi", 1, "8"))
        .isEqualTo(writeableFilemanValue("ien", 1, "8"));
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
