package gov.va.api.health.vistafhirquery.service.util;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

public class Translations {
  public static <F, T> Function<F, T> ignoreAndReturnNull() {
    return ignored -> null;
  }

  public static <F, T> Function<F, T> ignoreAndReturnValue(T value) {
    return ignored -> value;
  }

  public static <FromT, ToT> TranslationBuilder<FromT, ToT> of(Class<FromT> from, Class<ToT> to) {
    return new TranslationBuilder<FromT, ToT>();
  }

  public static TranslationBuilder<String, String> ofStringToString() {
    return of(String.class, String.class);
  }

  public static <ToT> TranslationBuilder<String, ToT> ofStringToType(Class<ToT> to) {
    return of(String.class, to);
  }

  public static <T> Supplier<T> returnNull() {
    return () -> null;
  }

  public static <T> Supplier<T> returnValue(T value) {
    return () -> value;
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static class BasicTranslation<FromT, ToT> implements Translation<FromT, ToT> {
    @NonNull private final Map<FromT, ToT> mappings;

    @NonNull private final Supplier<ToT> whenNullOrEmpty;

    @NonNull private final Function<FromT, ToT> whenNotFound;

    @Override
    public ToT lookup(FromT from) {
      if ((from == null) || (from instanceof CharSequence && isBlank((CharSequence) from))) {
        return whenNullOrEmpty.get();
      }
      var to = mappings.get(from);
      return to == null ? whenNotFound.apply(from) : to;
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TranslationBuilder<FromT, ToT> {
    private final FromPhrase fromPhrase = new FromPhrase();

    private final Map<FromT, ToT> mappings = new HashMap<>();

    @Setter private Supplier<ToT> whenNullOrEmpty;

    @Setter private Function<FromT, ToT> whenNotFound;

    /** Finalize the translations. */
    public Translation<FromT, ToT> build() {
      return new BasicTranslation<>(
          mappings,
          whenNullOrEmpty == null ? returnNull() : whenNullOrEmpty,
          whenNotFound == null ? ignoreAndReturnNull() : whenNotFound);
    }

    /** Translate from these values. */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final FromPhrase from(@NonNull FromT... from) {
      if (from.length == 0) {
        throw new IllegalArgumentException("Must specify at least one value to translate from.");
      }
      fromPhrase.nextMapping = from;
      return fromPhrase;
    }

    public class FromPhrase {
      private FromT[] nextMapping;

      /** Translate to this value. */
      public TranslationBuilder<FromT, ToT> to(ToT to) {
        for (FromT from : nextMapping) {
          mappings.put(from, to);
        }
        return TranslationBuilder.this;
      }
    }
  }
}
