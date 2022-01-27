package gov.va.api.health.vistafhirquery.service.util;

import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Predicates {
  private static final Predicate<?> SELECT_ALL = new SelectAll<>();

  /** Identical to selectAll but provides clearer intent in some situations. */
  public static <T> Predicate<T> noFilter() {
    return selectAll();
  }

  /** Return new predicate that always returns true. */
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> selectAll() {
    return (Predicate<T>) SELECT_ALL;
  }

  /** Return new predicate that always returns true. */
  public static <T> Predicate<T> selectAll(Class<T> ofType) {
    return selectAll();
  }

  private static class SelectAll<T> implements Predicate<T> {
    /** Optimized 'and' that detects instances of SelectAll and skips adding them to the chain. */
    @Override
    public Predicate<T> and(@NonNull Predicate<? super T> other) {
      if (other instanceof SelectAll<?>) {
        return this;
      }
      return Predicate.super.and(other);
    }

    /** Optimized 'or' that detects instances of SelectAll and skips adding them to the chain. */
    @Override
    public Predicate<T> or(@NonNull Predicate<? super T> other) {
      if (other instanceof SelectAll<?>) {
        return this;
      }
      return Predicate.super.or(other);
    }

    @Override
    public boolean test(T t) {
      return true;
    }
  }
}
