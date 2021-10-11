package gov.va.api.health.vistafhirquery.mockservices;

import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.contentTypeApplicationJson;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.fileContent;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.json;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.charon.api.RpcDetails;
import gov.va.api.lighthouse.charon.api.RpcDetails.Parameter;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.api.v1.RpcRequestV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;

/** Mock requests/results from the Lighthouse RPC Gateway. */
@Data
@RequiredArgsConstructor(staticName = "using")
@Slf4j
public class LhsLighthouseRpcGatewayMocksV1 implements MockService {
  private final int port;

  private List<String> supportedQueries = new ArrayList<>();

  private List<Consumer<MockServerClient>> supportedRequests = List.of(this::respondByFile);

  private void addSupportedQuery(RpcDetails body) {
    supportedQueries.add(
        "[POST] http://localhost:" + port() + "/v1/rpc with RPC Details like " + json(body));
  }

  @SuppressWarnings("UnnecessaryParentheses")
  @SneakyThrows
  private HttpResponse chooseResponseBasedOnFile(HttpRequest request) {
    ObjectMapper mapper = JacksonConfig.createMapper();
    RpcRequestV1 rpcRequest = mapper.readValue(request.getBodyAsString(), RpcRequestV1.class);
    log.info("PROCESSING RPC REQUEST: {}", rpcRequest);
    Parameter ap = rpcRequest.rpc().parameters().get(0);
    // Looking for param^FILE^literal^2.312
    var response =
        ap.array().stream()
            .filter(p -> p.startsWith("param^FILE^literal^") || "api^search^coverage".equals(p))
            .map(p -> p.replace("param^FILE^literal^", ""))
            .map(p -> p.replace("api^search^", ""))
            // File 2 only works here because the request is for fields of subfile .312
            .map(
                matcher ->
                    switch (matcher) {
                      case "2.312", "coverage" -> "/lhslighthouserpcgateway/"
                          + "response-coverage-search-by-patient.json";
                      case "36" -> "/lhslighthouserpcgateway/response-organization-read.json";
                      case "355.3" -> "/lhslighthouserpcgateway/response-insurance-plan-read.json";
                      case "365.12" -> "/lhslighthouserpcgateway"
                          + "/response-organization-payor-search.json";
                      default -> null;
                    })
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No response for: " + ap));
    return ok(response);
  }

  private HttpResponse ok(String resultsFile) {
    log.info("Responding with {}", resultsFile);
    return response()
        .withStatusCode(200)
        .withHeader(contentTypeApplicationJson())
        .withBody(rpcResponseOkWithContent(resultsFile));
  }

  void respondByFile(MockServerClient mock) {
    var details =
        RpcDetails.builder()
            .name(LhsLighthouseRpcGatewayGetsManifest.RPC_NAME)
            .context("LHS RPC CONTEXT")
            .build();
    addSupportedQuery(details);
    mock.when(rpcQueryWithExpectedRpcDetails(port(), details))
        .respond(this::chooseResponseBasedOnFile);
  }

  /** Create an HTTP Request for mocking a Vistalink API /rpc endpoint. */
  @SneakyThrows
  public HttpRequest rpcQueryWithExpectedRpcDetails(int port, RpcDetails rpcDetails) {
    var path = "/v1/rpc";
    log.info("Support Query [POST]: http://localhost:{}{}", port, path);
    URL url = new URL("http://localhost" + path);
    var request =
        request()
            .withMethod("POST")
            .withPath(url.getPath())
            .withHeader(contentTypeApplicationJson());
    if (rpcDetails != null) {
      String body = json(RpcRequestV1.builder().vista("673").rpc(rpcDetails).build());
      log.info("With RPC Details like: {}", body);
      request =
          request.withBody(
              JsonBody.json(body, StandardCharsets.UTF_8, MatchType.ONLY_MATCHING_FIELDS));
    }
    return request;
  }

  /** Return a json string representation of an RPC Response with an OK status. */
  public String rpcResponseOkWithContent(String rpcResponseFile) {
    String response =
        json(
            RpcInvocationResultV1.builder()
                .vista("673")
                .timezone("America/New_York")
                .response(fileContent(rpcResponseFile))
                .build());
    log.info("Respond with: {}", response);
    return response;
  }
}
