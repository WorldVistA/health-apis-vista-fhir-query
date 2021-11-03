package gov.va.api.health.vistafhirquery.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Map;
import org.junit.jupiter.api.Test;

class BiMapTest {

  @Test
  void duplicateMappingsGoesBoom() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> new BiMap<String, Integer>(Map.of("x", 1, "y", 2, "z", 1)));
  }

  @Test
  void leftToRight() {
    var m = new BiMap<String, Integer>(Map.of("x", 9, "y", 8, "z", 7));
    assertThat(m.leftToRight("x", 0)).isEqualTo(9);
    assertThat(m.leftToRight("y", 0)).isEqualTo(8);
    assertThat(m.leftToRight("nope", 666)).isEqualTo(666);
  }

  @Test
  void rightToLeft() {
    var m = new BiMap<String, Integer>(Map.of("x", 9, "y", 8, "z", 7));
    assertThat(m.rightToLeft(9, "-")).isEqualTo("x");
    assertThat(m.rightToLeft(7, "-")).isEqualTo("z");
    assertThat(m.rightToLeft(666, "-")).isEqualTo("-");
  }
}
