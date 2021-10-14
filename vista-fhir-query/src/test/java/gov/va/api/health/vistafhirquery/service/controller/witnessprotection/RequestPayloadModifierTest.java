package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageSamples;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;

class RequestPayloadModifierTest {
  static Coverage _coverageWithEmptyMeta() {
    return _coverageWithoutMeta().meta(Meta.builder().build());
  }

  static Coverage _coverageWithMetaSource(String source) {
    var coverage = _coverageWithEmptyMeta();
    coverage.meta().source(source);
    return coverage;
  }

  static Coverage _coverageWithoutMeta() {
    var coverage = CoverageSamples.R4.create().coverage();
    coverage.meta(null);
    return coverage;
  }

  static Stream<Arguments> modificationsAppliedWhenPostOrPutToSiteSpecificRequestUri() {
    return Stream.of(
        arguments("POST", "/hcs/123/r4/Coverage/999", _coverageWithoutMeta()),
        arguments("PUT", "/foo/hcs/123/r4/Coverage/999", _coverageWithEmptyMeta()),
        arguments("POST", "/foo/bar/hcs/123/r4/Coverage", _coverageWithMetaSource("")),
        arguments("PUT", "/hcs/123/r4/Coverage", _coverageWithMetaSource("666")),
        arguments("POST", "/hcs/123/r4/Coverage", _coverageWithMetaSource("123")));
  }

  private MockHttpServletRequest _request(String method, String uri) {
    var request = new MockHttpServletRequest();
    request.setMethod(method);
    request.setRequestURI(uri);
    return request;
  }

  @ParameterizedTest
  @MethodSource
  void modificationsAppliedWhenPostOrPutToSiteSpecificRequestUri(
      String method, String uri, Coverage payload) {
    RequestPayloadModifier.forPayload(payload)
        .addMeta(payload::meta)
        .request(_request(method, uri))
        .build()
        .applyModifications();
    assertThat(payload.meta()).isNotNull();
    assertThat(payload.meta().source()).isEqualTo("123");
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "DELETE", "HEAD"})
  void modificationsNotAppliedWhenNotPutOrPost(String method) {
    var payload = _coverageWithoutMeta();
    RequestPayloadModifier.forPayload(payload)
        .addMeta(payload::meta)
        .request(_request(method, "/hcs/123/r4/Coverage"))
        .build()
        .applyModifications();
    assertThat(payload.meta()).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "/r4/Coverage", "/nope/123/r4/Coverage", "/hcs/123"})
  @NullSource
  void modificationsNotAppliedWhenNotSiteSpecificRequestUri(String uri) {
    var payload = _coverageWithoutMeta();
    RequestPayloadModifier.forPayload(payload)
        .addMeta(payload::meta)
        .request(_request("POST", uri))
        .build()
        .applyModifications();
    assertThat(payload.meta()).isNull();
  }
}
