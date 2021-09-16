package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.Coding;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SubscriberToBeneficiaryRelationshipTest {
  private static Coding coding(String code) {
    return Coding.builder().code(code).build();
  }

  static Stream<Arguments> relationshipMappings() {
    return Stream.of(
        arguments("01", SubscriberToBeneficiaryRelationship.SPOUSE, coding("spouse")),
        arguments("18", SubscriberToBeneficiaryRelationship.SELF, coding("self")),
        arguments("19", SubscriberToBeneficiaryRelationship.CHILD, coding("child")),
        arguments("20", SubscriberToBeneficiaryRelationship.EMPLOYEE, coding("other")),
        arguments("29", SubscriberToBeneficiaryRelationship.SIGNIFICANT_OTHER, coding("other")),
        arguments("32", SubscriberToBeneficiaryRelationship.MOTHER, coding("parent")),
        arguments("33", SubscriberToBeneficiaryRelationship.FATHER, coding("parent")),
        arguments("39", SubscriberToBeneficiaryRelationship.DONOR, coding("other")),
        arguments("41", SubscriberToBeneficiaryRelationship.INJURED, coding("injured")),
        arguments("53", SubscriberToBeneficiaryRelationship.PARTNER, coding("common")),
        arguments("G8", SubscriberToBeneficiaryRelationship.OTHER, coding("other")));
  }

  @ParameterizedTest
  @MethodSource("relationshipMappings")
  void asCoding(String ignored, SubscriberToBeneficiaryRelationship sample, Coding expected) {
    assertThat(sample.asCoding().code()).isEqualTo(expected.code());
  }

  @ParameterizedTest
  @MethodSource("relationshipMappings")
  void forCode(String sample, SubscriberToBeneficiaryRelationship expected, Coding ignored) {
    assertThat(SubscriberToBeneficiaryRelationship.forCode(sample).orElse(null))
        .isEqualTo(expected);
  }

  @Test
  void noMatch() {
    assertThat(SubscriberToBeneficiaryRelationship.forCode("NOPE")).isEmpty();
    assertThat(SubscriberToBeneficiaryRelationship.fromCoding(coding("NOPE"))).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("relationshipMappings")
  void relationshipMappings(
      String ignored, SubscriberToBeneficiaryRelationship expected, Coding sample) {
    if ("other".equals(sample.code()) || "parent".equals(sample.code())) {
      // Going from fhir to vista is lossy
      expected = SubscriberToBeneficiaryRelationship.OTHER;
    }
    assertThat(SubscriberToBeneficiaryRelationship.fromCoding(sample).orElse(null))
        .isEqualTo(expected);
  }
}
