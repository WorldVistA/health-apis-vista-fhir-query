package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.vistafhirquery.service.controller.medicationdispense.MedicationDispenseId.MalformedId;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MedicationDispenseIdTest {
  static Stream<Arguments> formatOfToString() {
    return Stream.of(Arguments.of("sNp1+673+M33714:3110507", "sNp1+673+M33714:3110507"));
  }

  @ParameterizedTest
  @MethodSource
  void formatOfToString(String input, String result) {
    MedicationDispenseId medicationDispenseId = MedicationDispenseId.fromString(input);
    assertThat(medicationDispenseId.toString()).isEqualTo(result);
  }

  @Test
  void fromStringParsesFillDate() {
    var medicationDispenseId = MedicationDispenseId.fromString("sNp1+673+M33714:3110507");
    assertThat(medicationDispenseId.vistaId().toString()).isEqualTo("Np1+673+M33714");
    assertThat(medicationDispenseId.fillDate()).isEqualTo("3110507");
  }

  @Test
  void fromStringThrowsIfMissingFillDate() {
    assertThatExceptionOfType(MalformedId.class)
        .isThrownBy(() -> MedicationDispenseId.fromString("sNp1+673+M33714"));
  }
}
