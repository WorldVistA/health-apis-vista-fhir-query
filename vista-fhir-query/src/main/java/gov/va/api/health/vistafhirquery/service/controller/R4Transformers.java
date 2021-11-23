package gov.va.api.health.vistafhirquery.service.controller;

import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.fhir.api.IsReference;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.lighthouse.charon.models.FilemanDate;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/** Utility class for common transformations. */
@UtilityClass
public class R4Transformers {
  private static final Pattern BIG_DECIMAL_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

  /**
   * Return false if at least one value in the given list is a non-blank string, or a non-null
   * object.
   */
  public static boolean allBlank(Object... values) {
    for (Object v : values) {
      if (!isBlank(v)) {
        return false;
      }
    }
    return true;
  }

  /** Wrap a coding in a codeable concept. */
  public static CodeableConcept asCodeableConcept(Coding coding) {
    if (coding == null) {
      return null;
    }
    return CodeableConcept.builder().coding(List.of(coding)).build();
  }

  /** Checks if the given system value has a match in the given CodeableConcept's coding List. */
  public static boolean codeableconceptHasCodingSystem(CodeableConcept c, String system) {
    if (isBlank(c) || emptyToNull(c.coding()) == null) {
      return false;
    }
    return c.coding().stream().anyMatch(coding -> system.equals(coding.system()));
  }

  private static <T extends IsSiteCoordinates> Optional<T> coordinatesForReference(
      IsReference maybeReference, Function<String, T> toCoordinates) {
    var refId = referenceIdFromUri(maybeReference);
    if (refId.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(toCoordinates.apply(refId.get()));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  /** Filter null items and return null if the result is null or empty. */
  public static <T> List<T> emptyToNull(List<T> items) {
    if (isEmpty(items)) {
      return null;
    }
    List<T> filtered = items.stream().filter(Objects::nonNull).collect(Collectors.toList());
    return filtered.isEmpty() ? null : filtered;
  }

  /** Extract an extension whose url matches the given url. */
  public static Optional<Extension> extensionForSystem(
      List<Extension> extensions, String definingUrl) {
    if (isBlank(extensions)) {
      throw BadExtension.because(definingUrl + " : extension is null");
    }
    return extensions.stream().filter(extension -> definingUrl.equals(extension.url())).findFirst();
  }

  /** Checks if the given system value matches to the given Identifier's system field. */
  public static boolean identifierHasCodingSystem(Identifier i, String system) {
    if (i == null || isBlank(i.system())) {
      return false;
    }
    return system.equals(i.system());
  }

  /**
   * Return the result of the given extractor function if the given object is present. The object
   * will be passed to the apply method of the extractor function.
   *
   * <p>Consider this example:
   *
   * <pre>
   * {@code ifPresent(patient.getGender(), gender -> Patient.Gender.valueOf(gender.value()))}
   * </pre>
   *
   * This is equivalent to this standard Java code.
   *
   * <pre>{@code
   * Gender gender = patient.getGender();
   * if (gender == null) {
   *   return null;
   * } else {
   *   return Patient.Gender.valueOf(gender.value());
   * }
   * }</pre>
   */
  public static <T, R> R ifPresent(T object, Function<T, R> extract) {
    if (isBlank(object)) {
      return null;
    }
    return extract.apply(object);
  }

  /** Return true if the value is a blank string, or any other object that is null. */
  public static boolean isBlank(Object value) {
    if (value instanceof CharSequence) {
      return StringUtils.isBlank((CharSequence) value);
    }
    if (value instanceof Collection<?>) {
      return ((Collection<?>) value).isEmpty();
    }
    if (value instanceof Optional<?>) {
      return ((Optional<?>) value).isEmpty() || isBlank(((Optional<?>) value).get());
    }
    if (value instanceof Map<?, ?>) {
      return ((Map<?, ?>) value).isEmpty();
    }
    return value == null;
  }

  /** Returns true if Boolean is null or false . */
  public static boolean nullOrfalse(Boolean bool) {
    return null == bool || !bool;
  }

  /** Build an identifier from a patient icn, site, and vista record id. */
  public static String patientCoordinateStringFrom(String patientIcn, String site, String ien) {
    return PatientTypeCoordinates.builder().icn(patientIcn).site(site).ien(ien).build().toString();
  }

  /** Given a reference, get the id and try to parse it as patient coordinates. */
  public static Optional<PatientTypeCoordinates> patientCoordinatesForReference(
      IsReference maybeReference) {
    return coordinatesForReference(maybeReference, PatientTypeCoordinates::fromString);
  }

  /** Given a reference, get the id and try to parse it as record coordinates. */
  public static Optional<RecordCoordinates> recordCoordinatesForReference(
      IsReference maybeReference) {
    return coordinatesForReference(maybeReference, RecordCoordinates::fromString);
  }

  /** Given a reference, attempt to get the reference Id from the reference field. */
  public static Optional<String> referenceIdFromUri(IsReference maybeReference) {
    if (maybeReference == null || maybeReference.reference() == null) {
      return Optional.empty();
    }
    String reference = maybeReference.reference();
    String[] referenceParts = reference.split("/", -1);
    if (referenceParts.length <= 1) {
      return Optional.empty();
    }
    return Optional.ofNullable(referenceParts[referenceParts.length - 1]);
  }

  /** Creates a BigDecimal from a string if possible, otherwise returns null. */
  public static BigDecimal toBigDecimal(String string) {
    if (isBlank(string)) {
      return null;
    }
    if (BIG_DECIMAL_PATTERN.matcher(string).matches()) {
      return new BigDecimal(string);
    }
    return null;
  }

  /** Transform a FileMan date to a human date. */
  public static String toHumanDateTime(ValueOnlyXmlAttribute filemanDateTime) {
    FilemanDate result = FilemanDate.from(filemanDateTime, ZoneId.of("UTC"));
    if (isBlank(result)) {
      return null;
    }
    return result.instant().toString();
  }

  /** Transform an Instant to an Optional String. */
  public static Optional<String> toIso8601(Instant maybeInstant) {
    if (maybeInstant == null) {
      return Optional.empty();
    }
    return Optional.of(maybeInstant.toString());
  }

  /** Take an instant and from a local-fileman-date macro that charon can consume. */
  public static Optional<String> toLocalDateMacroString(Instant maybeInstant) {
    if (maybeInstant == null) {
      return Optional.empty();
    }
    return Optional.of(String.format("${local-fileman-date(%s)}", maybeInstant));
  }

  /** Create a reference sing the resourceType, an id, and a display. */
  public static Reference toReference(
      @NonNull String resourceType, String maybeId, String maybeDisplay) {
    if (allBlank(maybeId, maybeDisplay)) {
      return null;
    }
    return Reference.builder()
        .reference(ifPresent(maybeId, id -> resourceType + "/" + id))
        .display(maybeDisplay)
        .build();
  }

  public static Reference toReference(
      @NonNull String resourceType, @NonNull PatientTypeCoordinates coordinates) {
    return toReference(resourceType, coordinates.toString(), null);
  }

  public static Reference toReference(
      @NonNull String resourceType,
      @NonNull RecordCoordinates coordinates,
      Optional<String> maybeDisplay) {
    return toReference(resourceType, coordinates.toString(), maybeDisplay.orElse(null));
  }

  public static Reference toReference(
      @NonNull String resourceType, @NonNull RecordCoordinates coordinates) {
    return toReference(resourceType, coordinates, Optional.empty());
  }

  /** Build an Identifier Segment using patientId, siteId, and the recordId. */
  public static String toResourceId(
      String patientId, String siteId, VprGetPatientData.Domains recordDomain, String recordId) {
    if (isBlank(recordId)) {
      return null;
    }
    return SegmentedVistaIdentifier.builder()
        .patientIdentifierType(SegmentedVistaIdentifier.PatientIdentifierType.NATIONAL_ICN)
        .patientIdentifier(patientId)
        .siteId(siteId)
        .vprRpcDomain(recordDomain)
        .recordId(recordId)
        .build()
        .pack();
  }

  /** Gets value of a ValueOnlyXmlAttribute if it exists. */
  public static String valueOfValueOnlyXmlAttribute(ValueOnlyXmlAttribute valueOnlyXmlAttribute) {
    if (isBlank(valueOnlyXmlAttribute)) {
      return null;
    } else {
      return valueOnlyXmlAttribute.value();
    }
  }
}
