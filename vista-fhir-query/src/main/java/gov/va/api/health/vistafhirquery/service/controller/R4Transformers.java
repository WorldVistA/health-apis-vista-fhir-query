package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
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
    log.info("ToDo: Parse and transformer FileMan DateTimes.");
    String fm = valueOfValueOnlyXmlAttribute(filemanDateTime);
    return fm;
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

  /** Build an Identifier Segment using patientId, siteId, and the recordId. */
  public static String toResourceId(String patientId, String siteId, String recordId) {
    if (isBlank(recordId)) {
      return null;
    }
    return VistaIdentifierSegment.builder()
        .patientIdentifierType(VistaIdentifierSegment.PatientIdentifierType.NATIONAL_ICN)
        .patientIdentifier(patientId)
        .vistaSiteId(siteId)
        .vistaRecordId(recordId)
        .build()
        .toIdentifierSegment();
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
