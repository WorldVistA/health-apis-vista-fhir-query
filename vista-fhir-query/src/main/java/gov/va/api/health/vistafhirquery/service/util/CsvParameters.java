package gov.va.api.health.vistafhirquery.service.util;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This is a full CSV parser, but works fine for FHIR style CSV parameters. It does not support the
 * full RFC 4180 document, e.g. use of quotes.
 */
public class CsvParameters {

  private static String[] split(String csv) {
    return csv.split(",", -1);
  }

  /** Parse csv to an immutable list, defaulting to empty. */
  public static List<String> toList(String csv) {
    return toListOrDefault(csv, Collections::emptyList);
  }

  /** Parse csv to an immutable list, defaulting to the provided list. */
  public static List<String> toListOrDefault(String csv, Supplier<List<String>> defaultValue) {
    if (isBlank(csv)) {
      return defaultValue.get();
    }
    return Arrays.asList(split(csv));
  }

  /** Parse csv to an immutable set, default to empty. */
  public static Set<String> toSet(String csv) {
    return toSetOrDefault(csv, Collections::emptySet);
  }

  /** Parse csv to an immutable set, default to the provided set. */
  public static Set<String> toSetOrDefault(String csv, Supplier<Set<String>> defaultValue) {
    if (isBlank(csv)) {
      return defaultValue.get();
    }
    return Set.of(split(csv));
  }

  /** Parse csv to a stream, default to empty. */
  public static Stream<String> toStream(String csv) {
    return toStreamOrDefault(csv, Stream::empty);
  }

  /** Parse csv to a stream, default to the provided stream. */
  public static Stream<String> toStreamOrDefault(
      String csv, Supplier<Stream<String>> defaultValue) {
    if (isBlank(csv)) {
      return defaultValue.get();
    }
    return Stream.of(split(csv));
  }
}
