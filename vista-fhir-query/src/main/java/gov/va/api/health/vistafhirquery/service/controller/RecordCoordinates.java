package gov.va.api.health.vistafhirquery.service.controller;

import static java.lang.String.format;
import static java.lang.String.join;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class RecordCoordinates {
  @NonNull String site;

  @NonNull String file;

  @NonNull String ien;

  /**
   * Create a new entry, removing the trailing comma from the ien. E.g., "123,456," becomes
   * "123,456".
   */
  public RecordCoordinates(@NonNull String site, @NonNull String file, @NonNull String ien) {
    this.site = site;
    this.file = file;
    this.ien = endsWithTrailingComma(ien) ? removeTrailingComma(ien) : ien;
  }

  /** Try to parse a string value to an identifier. */
  public static RecordCoordinates fromString(String identifier) {
    String[] parts = identifier.split(";", -1);
    if (parts.length != 3) {
      throw new IllegalArgumentException(
          format(
              "Expected %s (%s) to have three ';' separated parts, but found %d.",
              RecordCoordinates.class.getSimpleName(), identifier, parts.length));
    }
    return RecordCoordinates.builder().site(parts[0]).file(parts[1]).ien(parts[2]).build();
  }

  private boolean endsWithTrailingComma(String ien) {
    return ien.length() > 1 && ien.charAt(ien.length() - 1) == ',';
  }

  private String removeTrailingComma(String ien) {
    return ien.substring(0, ien.length() - 1);
  }

  @Override
  public String toString() {
    return join(";", site(), file(), ien());
  }
}
