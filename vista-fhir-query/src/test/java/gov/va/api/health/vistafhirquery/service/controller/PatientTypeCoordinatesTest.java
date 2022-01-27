package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PatientTypeCoordinatesTest {
  @ParameterizedTest
  @ValueSource(strings = {"p1", "123+456", "p1+123+456+789+shank"})
  void identifierFromStringThrowsIllegalArgumentForBadValues(String badId) {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> PatientTypeCoordinates.fromString(badId, "ignored"));
  }

  @Test
  void roundTrip() {
    var sample = "p1+123+456+789";
    var expected =
        PatientTypeCoordinates.builder().icn("p1").site("123").file("456").ien("789").build();
    var pc = PatientTypeCoordinates.fromString(sample, "ignored");
    assertThat(pc).isEqualTo(expected);
    assertThat(pc.toString()).isEqualTo(sample);
  }

  @Test
  void roundTripUsingDefaultFile() {
    var sample = "p1+123+789";
    var pc = PatientTypeCoordinates.fromString(sample, "456");
    assertThat(pc)
        .isEqualTo(
            PatientTypeCoordinates.builder().icn("p1").site("123").file("456").ien("789").build());
    assertThat(pc.toString()).isEqualTo("p1+123+456+789");
  }
}
