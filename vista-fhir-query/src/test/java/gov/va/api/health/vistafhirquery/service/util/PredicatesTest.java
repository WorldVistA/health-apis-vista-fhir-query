package gov.va.api.health.vistafhirquery.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PredicatesTest {

  @Test
  void selectAllAlwaysReturnsTrue() {
    assertThat(Predicates.selectAll().test("foo")).isTrue();
    assertThat(
            Predicates.selectAll(String.class)
                .and(Predicates.selectAll())
                .or(Predicates.noFilter())
                .test("foo"))
        .isTrue();
    assertThat(Predicates.selectAll().or(o -> false).test("foo")).isTrue();
    assertThat(Predicates.selectAll().and(o -> false).test("foo")).isFalse();
  }
}
