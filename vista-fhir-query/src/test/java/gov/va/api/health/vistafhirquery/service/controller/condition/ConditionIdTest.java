package gov.va.api.health.vistafhirquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ConditionIdTest {
  static Stream<Arguments> formatOfToString() {
    return Stream.of(
        Arguments.of("sNp1+123+TT;2931013.07;23:391.2", "sNp1+123+TT;2931013.07;23:391.2"),
        Arguments.of("sNp1+123+PP;2931013.07;23", "sNp1+123+PP;2931013.07;23"));
  }

  @ParameterizedTest
  @MethodSource
  void formatOfToString(String input, String result) {
    var conditionIdVisit = ConditionId.fromString(input);
    assertThat(conditionIdVisit.toString()).isEqualTo(result);
  }

  @Test
  void problemId() {
    var conditionId = ConditionId.fromString("sNp1+123+PP;2931013.07;23");
    assertThat(conditionId.vistaId().toString()).isEqualTo("Np1+123+PP;2931013.07;23");
    assertThat(conditionId.icdCode().isEmpty()).isTrue();
  }

  @Test
  void visitsId() {
    var conditionId = ConditionId.fromString("sNp1+123+TT;2931013.07;23:391.2");
    assertThat(conditionId.vistaId().toString()).isEqualTo("Np1+123+TT;2931013.07;23");
    assertThat(conditionId.icdCode().get()).isEqualTo("391.2");
  }
}
