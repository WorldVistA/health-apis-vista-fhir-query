package gov.va.api.health.vistafhirquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties.Links;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties.UrlOrPathConfigurationException;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LinkPropertiesTest {

  static Stream<Arguments> resourceUrl() {
    return Stream.of(
        arguments(Patient.builder().id("p1").build(), "http://custom.com/foo/Patient/p1"),
        arguments(
            Immunization.builder().id("i1").build(), "http://also-custom.com/bar/Immunization/i1"),
        arguments(
            Condition.builder().id("c1").meta(Meta.builder().source("123").build()).build(),
            "http://default.com/site/123/r4/Condition/c1"));
  }

  Links _links() {
    return LinkProperties.builder()
        .publicUrl("http://default.com")
        .publicR4BasePath("site/{site}/r4")
        .customR4UrlAndPath(
            Map.of(
                "Patient", "http://custom.com/foo",
                "Immunization", "http://also-custom.com/bar"))
        .build()
        .r4();
  }

  @Test
  void customR4UrlDefaultsToEmpty() {
    assertThat(LinkProperties.builder().build().getCustomR4UrlAndPath()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource
  void resourceUrl(Resource resource, String expectedUrl) {
    assertThat(_links().readUrl(resource)).isEqualTo(expectedUrl);
  }

  @Test
  void resourceUrlWithoutSiteThrowsExceptionIfUrlHasPlaceholder() {
    assertThatExceptionOfType(UrlOrPathConfigurationException.class)
        .isThrownBy(() -> _links().resourceUrlWithoutSite("Coverage"));
  }
}
