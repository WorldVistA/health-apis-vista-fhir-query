package gov.va.api.health.vistafhirquery.service.util;

import static gov.va.api.health.vistafhirquery.service.util.Translations.ignoreAndReturnNull;
import static gov.va.api.health.vistafhirquery.service.util.Translations.ignoreAndReturnValue;
import static gov.va.api.health.vistafhirquery.service.util.Translations.returnNull;
import static gov.va.api.health.vistafhirquery.service.util.Translations.returnValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class TranslationsTest {

  @Test
  void fromTo() {
    var lettersToNumbers =
        // Translations.of(String.class, Integer.class)
        Translations.ofStringToType(Integer.class)
            .from("a", "A", "one")
            .to(1)
            .from("b", "B")
            .to(22)
            .from("c", "C")
            .to(33)
            .whenNullOrEmpty(returnValue(86))
            .whenNotFound(ignoreAndReturnValue(101))
            .build();

    assertThat(lettersToNumbers.lookup("a")).isEqualTo(1);
    assertThat(lettersToNumbers.lookup("A")).isEqualTo(1);
    assertThat(lettersToNumbers.lookup("one")).isEqualTo(1);
    assertThat(lettersToNumbers.lookup("b")).isEqualTo(22);
    assertThat(lettersToNumbers.lookup("B")).isEqualTo(22);
    assertThat(lettersToNumbers.lookup("c")).isEqualTo(33);
    assertThat(lettersToNumbers.lookup("C")).isEqualTo(33);
    assertThat(lettersToNumbers.lookup(null)).isEqualTo(86);
    assertThat(lettersToNumbers.lookup("")).isEqualTo(86);
    assertThat(lettersToNumbers.lookup("NOPE")).isEqualTo(101);
  }

  @Test
  void whenNotFoundReturnNull() {
    var tx =
        Translations.of(Integer.class, String.class)
            .from(1)
            .to("A")
            .whenNullOrEmpty(returnNull())
            .whenNotFound(ignoreAndReturnNull())
            .build();

    assertThat(tx.lookup(99)).isNull();
    assertThat(tx.lookup(null)).isNull();
  }

  @Test
  void whenNotFoundReturnValue() {
    var tx =
        Translations.of(Integer.class, String.class)
            .from(1)
            .to("A")
            .whenNullOrEmpty(returnValue("is null"))
            .whenNotFound(ignoreAndReturnValue("not found"))
            .build();

    assertThat(tx.lookup(99)).isEqualTo("not found");
    assertThat(tx.lookup(null)).isEqualTo("is null");
  }

  @Test
  void whenNotFoundThrowException() {
    var tx =
        Translations.of(Integer.class, String.class)
            .from(1)
            .to("A")
            .whenNullOrEmpty(returnNull())
            .whenNotFound(
                missing -> {
                  throw new FugaziException("missing " + missing);
                })
            .build();

    assertThatExceptionOfType(FugaziException.class)
        .isThrownBy(() -> tx.lookup(99))
        .withMessage("missing 99");
  }

  private static class FugaziException extends RuntimeException {
    public FugaziException(String message) {
      super(message);
    }
  }
}
