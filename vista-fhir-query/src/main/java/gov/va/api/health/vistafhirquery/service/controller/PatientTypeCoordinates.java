package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static java.lang.String.format;
import static java.lang.String.join;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PatientTypeCoordinates implements IsSiteCoordinates {
  @NonNull String icn;

  @NonNull String site;

  @NonNull String file;

  @NonNull String ien;

  /** Try to parse a string value to an identifier. */
  public static PatientTypeCoordinates fromString(String identifier, String defaultFile) {
    if (isBlank(defaultFile)) {
      throw new IllegalStateException(
          "Default file must be specified to parse the "
              + PatientTypeCoordinates.class.getSimpleName());
    }
    String[] parts = identifier.split("\\+", -1);
    if (parts.length == 4) {
      return PatientTypeCoordinates.builder()
          .icn(parts[0])
          .site(parts[1])
          .file(parts[2])
          .ien(parts[3])
          .build();
    }
    if (parts.length == 3) {
      return PatientTypeCoordinates.builder()
          .icn(parts[0])
          .site(parts[1])
          .file(defaultFile)
          .ien(parts[2])
          .build();
    }
    throw new IllegalArgumentException(
        format(
            "Expected %s (%s) to have 3 or 4 '+' separated parts, but found %d.",
            PatientTypeCoordinates.class.getSimpleName(), identifier, parts.length));
  }

  @Override
  public String toString() {
    return join("+", icn(), site(), file(), ien());
  }
}
