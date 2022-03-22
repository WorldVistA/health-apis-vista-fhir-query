package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Narrative;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToCreateDuplicateRecord;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.CannotUpdateResourceWithMismatchedIds;
import gov.va.api.health.vistafhirquery.service.mpifhirqueryclient.MpiFhirQueryClientExceptions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

public class WebExceptionHandlerTest {
  private OperationOutcome _addDetails(OperationOutcome oo, String code, String display) {
    oo.issue()
        .get(0)
        .details(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/operation-outcome")
                            .code(code)
                            .display(display)
                            .build()))
                .build());
    return oo;
  }

  private WebExceptionHandler _handler() {
    return new WebExceptionHandler("");
  }

  private OperationOutcome _operationOutcome(String code) {
    return OperationOutcome.builder()
        .resourceType("OperationOutcome")
        .text(
            Narrative.builder()
                .status(Narrative.NarrativeStatus.additional)
                .div("<div>Failure: /fugazi</div>")
                .build())
        .issue(
            List.of(
                OperationOutcome.Issue.builder()
                    .severity(OperationOutcome.Issue.IssueSeverity.fatal)
                    .code(code)
                    .build()))
        .build();
  }

  private OperationOutcome _removeIdAndExtension(OperationOutcome outcome) {
    return outcome.id(null).extension(null);
  }

  private HttpServletRequest _request() {
    var request = new MockHttpServletRequest();
    request.setRequestURI("/fugazi");
    return request;
  }

  void assertHasMessageExtension(OperationOutcome oo, boolean hasIt) {
    assertThat(oo.extension().stream().anyMatch(e -> "message".equals(e.url()))).isEqualTo(hasIt);
  }

  @Test
  void badRequest() {
    var oo =
        _handler()
            .handleBadRequest(
                new UnsatisfiedServletRequestParameterException(
                    new String[] {"hello"}, Map.of("foo", new String[] {"bar"})),
                _request());
    assertHasMessageExtension(oo, true);
    assertThat(_removeIdAndExtension(oo))
        .usingRecursiveComparison()
        .isEqualTo(_operationOutcome("structure"));
  }

  @Test
  void cannotUpdateResourceWithMismatchedIds() {
    // expect 400
    // code: processing
    // coding: MSG_RESOURCE_ID_MISSING (if resource.id == null)
    // coding: MSG_RESOURCE_ID_MISMATCH (if url != resource.id)
    var ooNotSameId =
        _handler()
            .handleCannotUpdateResourceWithMismatchedIds(
                CannotUpdateResourceWithMismatchedIds.because("123", "456"), _request());
    assertHasMessageExtension(ooNotSameId, false);
    assertThat(_removeIdAndExtension(ooNotSameId))
        .usingRecursiveComparison()
        .isEqualTo(
            _addDetails(
                _operationOutcome("processing"),
                "MSG_RESOURCE_ID_MISMATCH",
                "Resource Id Mismatch"));
    var ooNotPresentInResource =
        _handler()
            .handleCannotUpdateResourceWithMismatchedIds(
                CannotUpdateResourceWithMismatchedIds.because("123", null), _request());
    assertHasMessageExtension(ooNotPresentInResource, false);
    assertThat(_removeIdAndExtension(ooNotPresentInResource))
        .usingRecursiveComparison()
        .isEqualTo(
            _addDetails(
                _operationOutcome("processing"), "MSG_RESOURCE_ID_MISSING", "Resource Id Missing"));
  }

  @Test
  void forbidden() {
    HttpClientErrorException forbidden =
        HttpClientErrorException.Forbidden.create(HttpStatus.FORBIDDEN, null, null, null, null);
    var oo = _handler().handleInternalServerError(forbidden, _request());
    assertHasMessageExtension(oo, true);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("exception"));
  }

  @Test
  void internalServerError() {
    HttpServerErrorException internalServerError =
        HttpServerErrorException.InternalServerError.create(
            HttpStatus.INTERNAL_SERVER_ERROR, null, null, null, null);
    var oo = _handler().handleInternalServerError(internalServerError, _request());
    assertHasMessageExtension(oo, false);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("exception"));
  }

  @Test
  void mfqRequestFailed() {
    MpiFhirQueryClientExceptions.MpiFhirQueryRequestFailed mfqRequestFailed =
        MpiFhirQueryClientExceptions.MpiFhirQueryRequestFailed.because("rip");
    var oo = _handler().mpiFhirQueryRequestFailed(mfqRequestFailed, _request());
    assertHasMessageExtension(oo, false);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("exception"));
  }

  @Test
  void notAllowed() {
    var oo =
        _handler()
            .handleNotAllowed(new HttpRequestMethodNotSupportedException("method"), _request());
    assertHasMessageExtension(oo, true);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("not-allowed"));
  }

  @Test
  void notFound() {
    var oo = _handler().handleNotFound(new ResourceExceptions.NotFound("x"), _request());
    assertHasMessageExtension(oo, true);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("not-found"));
  }

  @Test
  void requestTimeout() {
    ResourceAccessException requestTimeout = new ResourceAccessException(null);
    var oo = _handler().handleRequestTimeout(requestTimeout, _request());
    assertHasMessageExtension(oo, false);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("request-timeout"));
  }

  @Test
  void sanitizedMessage_exception() {
    assertThat(WebExceptionHandler.sanitizedMessage(new RuntimeException("oh noez")))
        .isEqualTo("oh noez");
  }

  @Test
  void sanitizedMessage_jsonEOFException() {
    JsonEOFException ex = mock(JsonEOFException.class);
    when(ex.getLocation()).thenReturn(new JsonLocation(null, 0, 0, 0));
    assertThat(WebExceptionHandler.sanitizedMessage(ex)).isEqualTo("line: 0, column: 0");
  }

  @Test
  void sanitizedMessage_jsonMappingException() {
    JsonMappingException ex = mock(JsonMappingException.class);
    when(ex.getPathReference()).thenReturn("x");
    assertThat(WebExceptionHandler.sanitizedMessage(ex)).isEqualTo("path: x");
  }

  @Test
  void sanitizedMessage_jsonParseException() {
    JsonParseException ex = mock(JsonParseException.class);
    when(ex.getLocation()).thenReturn(new JsonLocation(null, 0, 0, 0));
    assertThat(WebExceptionHandler.sanitizedMessage(ex)).isEqualTo("line: 0, column: 0");
  }

  @Test
  void sanitizedMessage_mismatchedInputException() {
    MismatchedInputException ex = mock(MismatchedInputException.class);
    when(ex.getPathReference()).thenReturn("path");
    assertThat(WebExceptionHandler.sanitizedMessage(ex)).isEqualTo("path: path");
  }

  @Test
  void snafu_json() {
    var oo =
        _handler().handleSnafu(new JsonParseException(mock(JsonParser.class), "x"), _request());
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("database"));
  }

  @Test
  void unauthorized() {
    HttpClientErrorException unauthorized =
        HttpClientErrorException.Unauthorized.create(
            HttpStatus.UNAUTHORIZED, null, null, null, null);
    var oo = _handler().handleUnauthorized(unauthorized, _request());
    assertHasMessageExtension(oo, true);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("unauthorized"));
  }

  @Test
  void unsupportedMediaType() {
    HttpMediaTypeNotSupportedException unsupportedType =
        new HttpMediaTypeNotSupportedException("unsupported type");
    var oo = _handler().handleUnsupportedMediaType(unsupportedType, _request());
    assertHasMessageExtension(oo, false);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("not-supported"));
  }

  @Test
  void validationException() {
    Set<ConstraintViolation<Foo>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(Foo.builder().build());
    var oo =
        _handler()
            .handleValidationException(new ConstraintViolationException(violations), _request());
    assertHasMessageExtension(oo, false);
    assertThat(_removeIdAndExtension(oo))
        .isEqualTo(
            OperationOutcome.builder()
                .resourceType("OperationOutcome")
                .text(
                    Narrative.builder()
                        .status(Narrative.NarrativeStatus.additional)
                        .div("<div>Failure: /fugazi</div>")
                        .build())
                .issue(
                    List.of(
                        OperationOutcome.Issue.builder()
                            .severity(OperationOutcome.Issue.IssueSeverity.fatal)
                            .code("structure")
                            .diagnostics("bar must not be null")
                            .build()))
                .build());
  }

  @Test
  void vistaRecordAlreadyExists() {
    var duplicate =
        AttemptToCreateDuplicateRecord.builder().recordType("Fugazi").errorData(Map.of()).build();
    var oo = _handler().handleUnprocessableEntity(duplicate, _request());
    assertHasMessageExtension(oo, true);
    assertThat(_removeIdAndExtension(oo)).isEqualTo(_operationOutcome("structure"));
  }

  @Value
  @Builder
  private static final class Foo {
    @NotNull String bar;
  }
}
