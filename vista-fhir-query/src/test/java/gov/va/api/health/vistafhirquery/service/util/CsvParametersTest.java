package gov.va.api.health.vistafhirquery.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class CsvParametersTest {

  static Stream<Arguments> csvValues() {
    return Stream.of(
        Arguments.of("foo", List.of("foo")),
        Arguments.of("foo,bar", List.of("foo", "bar")),
        Arguments.of("foo,bar,123", List.of("foo", "bar", "123")),
        Arguments.of("foo,", List.of("foo", "")),
        Arguments.of(",foo", List.of("", "foo")));
  }

  @ParameterizedTest
  @MethodSource("csvValues")
  void parse(String csv, Collection<String> values) {
    assertThat(CsvParameters.toList(csv)).containsExactlyElementsOf(values);
    assertThat(CsvParameters.toStream(csv)).containsExactlyElementsOf(values);
    assertThat(CsvParameters.toSet(csv)).containsExactlyInAnyOrderElementsOf(values);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void parseEmpty(String empty) {
    assertThat(CsvParameters.toListOrDefault(empty, () -> List.of("a", "b")))
        .containsExactly("a", "b");
    assertThat(CsvParameters.toStreamOrDefault(empty, () -> Stream.of("a", "b")))
        .containsExactly("a", "b");
    assertThat(CsvParameters.toSetOrDefault(empty, () -> Set.of("a", "b")))
        .containsExactlyInAnyOrder("a", "b");
  }
}
