package gov.va.api.health.vistafhirquery.service.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(staticName = "forFile")
public class WriteableFilemanValueFactory {
  @NonNull @Getter private final String file;

  /**
   * Create an index supplier that will automatically increment by 1. The first index returned is 1.
   */
  public static Supplier<Integer> autoincrement() {
    AtomicInteger index = new AtomicInteger(0);
    return index::incrementAndGet;
  }

  /** Create a constant index. */
  public static Supplier<Integer> index(int value) {
    return () -> value;
  }

  /** Build a WriteableFilemanValue from a Boolean value. */
  public Optional<WriteableFilemanValue> forBoolean(
      @NonNull String field, int index, Boolean value, @NonNull Function<Boolean, String> mapper) {
    try {
      return forString(field, index, mapper.apply(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /** Build a WriteableFilemanValue using the coding.code field. */
  public Optional<WriteableFilemanValue> forCoding(String field, int index, Coding coding) {
    if (isBlank(coding.code())) {
      return Optional.empty();
    }
    return forString(field, index, coding.code());
  }

  /** Build a WriteableFilemanValue using the identifer.value field. */
  public Optional<WriteableFilemanValue> forIdentifier(
      String field, int index, Identifier identifier) {
    if (identifier == null) {
      return Optional.empty();
    }
    return forString(field, index, identifier.value());
  }

  /** Build a WriteableFilemanValue from an Integer value. */
  public Optional<WriteableFilemanValue> forInteger(
      @NonNull String field, int index, Integer value) {
    if (value == null) {
      return Optional.empty();
    }
    return forString(field, index, "" + value);
  }

  /**
   * Creates a pointer using the Dynamic IEN Macro to a parent file (e.g.
   * 355.321^1^IEN^${355.32^1^IEN}).
   */
  public Optional<WriteableFilemanValue> forParentFileUsingIenMacro(
      int index, @NonNull String parentFileNumber, int parentFileIndex) {
    return forString("IEN", index, "${" + parentFileNumber + "^" + parentFileIndex + "^IEN}");
  }

  /** Build a WriteableFilemanValue pointer for the given file and index. */
  public Optional<WriteableFilemanValue> forPointer(@NonNull String file, int index, String ien) {
    if (isBlank(ien)) {
      return Optional.empty();
    }
    return Optional.of(
        WriteableFilemanValue.builder().file(file).field("ien").index(index).value(ien).build());
  }

  /** Build a WriteableFilemanValue with a grav?? marker added to the value. */
  public Optional<WriteableFilemanValue> forPointerWithGraveMarker(
      @NonNull String field, int index, String value) {
    if (isBlank(value)) {
      return Optional.empty();
    }
    return forString(field, index, "`" + value);
  }

  /** Build an optional WriteableFilemanValue for a string. */
  public Optional<WriteableFilemanValue> forString(@NonNull String field, int index, String value) {
    if (isBlank(value)) {
      return Optional.empty();
    }
    return Optional.of(
        WriteableFilemanValue.builder()
            .file(file())
            .index(index)
            .field(field)
            .value(value)
            .build());
  }

  public ToFilemanValue<PatientTypeCoordinates> patientTypeCoordinatesToPointer(
      String file, Supplier<Integer> index) {
    return coordinates -> forPointer(file, index.get(), coordinates.ien()).orElse(null);
  }

  public ToFilemanValue<RecordCoordinates> recordCoordinatesToPointer(
      String file, Supplier<Integer> index) {
    return coordinates -> forPointer(file, index.get(), coordinates.ien()).orElse(null);
  }

  public <T> ToFilemanValue<T> toString(
      String field, Supplier<Integer> index, Function<T, String> valueOf) {
    return (T item) -> forString(field, index.get(), valueOf.apply(item)).orElse(null);
  }

  @FunctionalInterface
  interface ToFilemanValue<T> extends Function<T, WriteableFilemanValue> {}
}
