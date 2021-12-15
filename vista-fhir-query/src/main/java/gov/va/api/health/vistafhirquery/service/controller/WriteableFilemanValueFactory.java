package gov.va.api.health.vistafhirquery.service.controller;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
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

  public ToFilemanValue<Extension> extensionToInteger(String field, Supplier<Integer> index) {
    return extension -> forInteger(field, index.get(), extension).orElse(null);
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

  /** Build a WriteableFilemanValue from an Integer value. */
  public Optional<WriteableFilemanValue> forInteger(
      @NonNull String field, int index, Integer value) {
    if (value == null) {
      return Optional.empty();
    }
    return forString(field, index, "" + value);
  }

  /** Build a WriteableFilemanValue using the extension.valueInteger field. */
  public Optional<WriteableFilemanValue> forInteger(
      @NonNull String field, int index, Extension extension) {
    if (extension == null) {
      return Optional.empty();
    }
    return forInteger(field, index, extension.valueInteger());
  }

  /** Build a WriteableFilemanValue pointer for the given file and index. */
  public Optional<WriteableFilemanValue> forPointer(@NonNull String file, int index, String ien) {
    if (isBlank(ien)) {
      return Optional.empty();
    }
    return Optional.of(
        WriteableFilemanValue.builder().file(file).field("ien").index(index).value(ien).build());
  }

  /**
   * Build a WriteableFilemanValue using the codeableConcept.coding.code field for a given system.
   */
  public WriteableFilemanValue forRequiredCodeableConcept(
      String field, int index, CodeableConcept codeableConcept, String codingSystem) {
    if (R4Transformers.isBlank(codeableConcept)
        || R4Transformers.isBlank(codeableConcept.coding())) {
      throw BadRequestPayload.because(
          file(), field, "CodeableConcept or Coding is missing for system: " + codingSystem);
    }
    var codingsForSystem =
        codeableConcept.coding().stream().filter(c -> codingSystem.equals(c.system())).toList();
    if (codingsForSystem.size() == 1) {
      return forRequiredCoding(field, index, codingsForSystem.get(0));
    }
    throw BadRequestPayload.because(
        file(),
        field,
        format(
            "Unexpected number of codings for system %s: %d",
            codingSystem, codingsForSystem.size()));
  }

  /** Build a WriteableFilemanValue using the coding.code field. */
  public WriteableFilemanValue forRequiredCoding(String field, int index, Coding coding) {
    if (isBlank(coding.code())) {
      throw BadRequestPayload.because(file(), field, ".coding.code is blank.");
    }
    return forString(field, index, coding.code())
        .orElseThrow(
            () -> BadRequestPayload.because(file(), field, "Required string value is blank."));
  }

  /** Build a WriteableFilemanValue using the identifer.value field. */
  public WriteableFilemanValue forRequiredIdentifier(
      String field, int index, Identifier identifier) {
    if (identifier == null || isBlank(identifier.value())) {
      throw BadRequestPayload.because(file(), field, "identifier or identifier.value is null.");
    }
    return forString(field, index, identifier.value())
        .orElseThrow(
            () -> BadRequestPayload.because(file(), field, "Required string value is blank."));
  }

  /**
   * Creates a pointer using the Dynamic IEN Macro to a parent file (e.g.
   * 355.321^1^IEN^${355.32^1^IEN}).
   */
  public WriteableFilemanValue forRequiredParentFileUsingIenMacro(
      int index, @NonNull String parentFileNumber, int parentFileIndex) {
    return forString("IEN", index, "${" + parentFileNumber + "^" + parentFileIndex + "^IEN}")
        .orElseThrow(
            () -> BadRequestPayload.because(file(), "IEN", "Required string value is blank."));
  }

  /** Build a WriteableFilemanValue with a grav?? marker added to the value. */
  public WriteableFilemanValue forRequiredPointerWithGraveMarker(
      @NonNull String field, int index, String value) {
    if (isBlank(value)) {
      throw BadRequestPayload.because(file(), field, "Required pointer was found to be null.");
    }
    return forString(field, index, "`" + value)
        .orElseThrow(
            () -> BadRequestPayload.because(file(), field, "Required string value is blank."));
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
