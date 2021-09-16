package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static java.util.Map.entry;

import gov.va.api.health.r4.api.datatypes.Coding;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public enum SubscriberToBeneficiaryRelationship {
  SPOUSE("01", "SPOUSE"),
  SELF("18", "SELF"),
  CHILD("19", "CHILD"),
  EMPLOYEE("20", "EMPLOYEE"),
  SIGNIFICANT_OTHER("29", "SIGNIFICANT OTHER"),
  MOTHER("32", "MOTHER"),
  FATHER("33", "FATHER"),
  DONOR("39", "ORGAN DONOR"),
  INJURED("41", "INJURED PLAINTIFF"),
  PARTNER("53", "LIFE PARTNER"),
  OTHER("G8", "OTHER RELATIONSHIP");

  private static final Map<SubscriberToBeneficiaryRelationship, Coding> RELATIONSHIP_TO_CODING =
      populateCodings();

  @Getter private final String code;

  @Getter private final String display;

  static Optional<SubscriberToBeneficiaryRelationship> forCode(@NonNull String value) {
    var matchedValues =
        Arrays.stream(SubscriberToBeneficiaryRelationship.values())
            .filter(r -> value.equals(r.code()))
            .toList();
    if (matchedValues.size() > 1) {
      throw new IllegalStateException("Code returned more than one value.");
    }
    if (matchedValues.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(matchedValues.get(0));
  }

  static Optional<SubscriberToBeneficiaryRelationship> fromCoding(@NonNull Coding coding) {
    var matchedCodings =
        RELATIONSHIP_TO_CODING.entrySet().stream()
            .filter(e -> e.getValue().code().equals(coding.code()))
            .map(Map.Entry::getKey)
            .toList();
    /*
     * From KBS team: "CodeableConcept won’t help figure out whether ‘parent’ means mother or
     * father. I think it has to be the lossy one: 'G8' FOR OTHER RELATIONSHIP;"
     */
    if (matchedCodings.size() > 1) {
      return Optional.of(OTHER);
    }
    if (matchedCodings.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(matchedCodings.get(0));
  }

  private static Map<SubscriberToBeneficiaryRelationship, Coding> populateCodings() {
    var other = subscriberRelationshipCoding("other", "Other");
    return Map.ofEntries(
        entry(SPOUSE, subscriberRelationshipCoding("spouse", "Spouse")),
        entry(SELF, subscriberRelationshipCoding("self", "Self")),
        entry(CHILD, subscriberRelationshipCoding("child", "Child")),
        entry(EMPLOYEE, other),
        entry(SIGNIFICANT_OTHER, other),
        entry(MOTHER, subscriberRelationshipCoding("parent", "Parent")),
        entry(FATHER, subscriberRelationshipCoding("parent", "Parent")),
        entry(DONOR, other),
        entry(INJURED, subscriberRelationshipCoding("injured", "Injured Party")),
        entry(PARTNER, subscriberRelationshipCoding("common", "Common Law Spouse")),
        entry(OTHER, other));
  }

  /** Build a coding using the subscriber relationship code system. */
  private static Coding subscriberRelationshipCoding(String code, String display) {
    return Coding.builder()
        .system("http://terminology.hl7.org/CodeSystem/subscriber-relationship")
        .code(code)
        .display(display)
        .build();
  }

  Coding asCoding() {
    var coding = RELATIONSHIP_TO_CODING.get(this);
    return copyOf(coding);
  }

  private Coding copyOf(Coding coding) {
    return Coding.builder()
        .system(coding.system())
        .code(coding.code())
        .display(coding.display())
        .build();
  }
}
