package gov.va.api.health.vistafhirquery.service.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
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

  public Function<Extension, WriteableFilemanValue> extensionToInteger(
      String field, Supplier<Integer> index) {
    return extension -> forInteger(field, index.get(), extension);
  }

  /** Build a WriteableFilemanValue from a Boolean value. */
  public WriteableFilemanValue forBoolean(String field, int index, Boolean value) {
    if (value == null) {
      return null;
    }
    return forString(field, index, value ? "YES" : "NO");
  }

  /** Build a WriteableFilemanValue using the extension.valueBoolean field. */
  public WriteableFilemanValue forBoolean(String field, int index, Extension extension) {
    if (extension == null) {
      return null;
    }
    return forBoolean(field, index, extension.valueBoolean());
  }

  /** Build a WriteableFilemanValue using the identifer.value field. */
  public WriteableFilemanValue forIdentifier(String field, int index, Identifier identifier) {
    if (identifier == null) {
      return null;
    }
    return forString(field, index, identifier.value());
  }

  /** Build a WriteableFilemanValue from an Integer value. */
  public WriteableFilemanValue forInteger(String field, int index, Integer value) {
    if (value == null) {
      return null;
    }
    return forString(field, index, "" + value);
  }

  /** Build a WriteableFilemanValue using the extension.valueInteger field. */
  public WriteableFilemanValue forInteger(String field, int index, Extension extension) {
    if (extension == null) {
      return null;
    }
    return forInteger(field, index, extension.valueInteger());
  }

  /** Build a WriteableFilemanValue pointer for the given file and index. */
  public WriteableFilemanValue forPointer(@NonNull String file, int index, String ien) {
    if (isBlank(ien)) {
      return null;
    }
    return WriteableFilemanValue.builder().file(file).field("ien").index(index).value(ien).build();
  }

  /** Build a WriteableFilemanValue with a default index of 1. */
  public WriteableFilemanValue forString(@NonNull String field, int index, String value) {
    if (isBlank(value)) {
      return null;
    }
    // For the time being, we support writing one resource at a time so the index will always be 1
    return WriteableFilemanValue.builder()
        .file(file())
        .index(index)
        .field(field)
        .value(value)
        .build();
  }

  public Function<PatientTypeCoordinates, WriteableFilemanValue> patientTypeCoordinatesToPointer(
      String file, Supplier<Integer> index) {
    return coordinates -> forPointer(file, index.get(), coordinates.ien());
  }

  public Function<RecordCoordinates, WriteableFilemanValue> recordCoordinatesToPointer(
      String file, Supplier<Integer> index) {
    return coordinates -> forPointer(file, index.get(), coordinates.ien());
  }

  public <T> Function<T, WriteableFilemanValue> toString(
      String field, Supplier<Integer> index, Function<T, String> valueOf) {
    return (T item) -> forString(field, index.get(), valueOf.apply(item));
  }
}
