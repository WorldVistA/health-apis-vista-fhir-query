package gov.va.api.health.vistafhirquery.mockservices;

import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.contentTypeApplicationJson;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.json;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.rpcQueryV0_WithExpectedRpcDetails;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.rpcResponseV0_OkWithContent;
import static org.mockserver.model.HttpResponse.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.charon.api.RpcDetails;
import gov.va.api.lighthouse.charon.api.RpcDetails.Parameter;
import gov.va.api.lighthouse.charon.api.RpcRequest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

/** Mock requests/results from the Lighthouse RPC Gateway. */
@Data
@Slf4j
@RequiredArgsConstructor(staticName = "using")
public class LhsLighthouseRpcGatewayMocksV0 implements MockService {
  private final int port;

  private List<String> supportedQueries = new ArrayList<>();

  private List<Consumer<MockServerClient>> supportedRequests = List.of(this::respondByFile);

  @SneakyThrows
  @SuppressWarnings("UnnecessaryParentheses")
  private static HttpResponse chooseResponseBasedOnFile(HttpRequest request) {
    ObjectMapper mapper = JacksonConfig.createMapper();
    var rpcRequest = mapper.readValue(request.getBodyAsString(), RpcRequest.class);
    log.info("PROCESSING RPC REQUEST: {}", rpcRequest);
    Parameter ap = rpcRequest.rpc().parameters().get(0);
    // Looking for param^FILE^literal^2.312
    // File 2 only works here because the request is for fields of subfile .312
    var response =
        ap.array().stream()
            .filter(p -> p.startsWith("param^FILE^literal^") || "api^search^coverage".equals(p))
            .map(p -> p.replace("param^FILE^literal^", ""))
            .map(p -> p.replace("api^search^", ""))
            .map(
                matcher ->
                    switch (matcher) {
                      case "36" -> "/lhslighthouserpcgateway/response-organization-read.json";
                      default -> null;
                    })
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No response for: " + ap));
    log.info("Responding with {}", response);
    return response()
        .withStatusCode(200)
        .withHeader(contentTypeApplicationJson())
        .withBody(rpcResponseV0_OkWithContent(response));
  }

  void respondByFile(MockServerClient mock) {
    var details =
        RpcDetails.builder()
            .name(LhsLighthouseRpcGatewayGetsManifest.RPC_NAME)
            .context("LHS RPC CONTEXT")
            .build();
    supportedQueries.add(
        "[POST] http://localhost:" + port() + "/rpc with RPC Details like " + json(details));
    mock.when(rpcQueryV0_WithExpectedRpcDetails(port(), details))
        .respond(LhsLighthouseRpcGatewayMocksV0::chooseResponseBasedOnFile);
  }
}
