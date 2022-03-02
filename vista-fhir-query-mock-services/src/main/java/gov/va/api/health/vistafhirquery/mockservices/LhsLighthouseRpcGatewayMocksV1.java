package gov.va.api.health.vistafhirquery.mockservices;

import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.contentTypeApplicationJson;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.json;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.rpcInvocationResultV1_OkWithContent;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.rpcQueryV1_WithExpectedRpcDetails;
import static org.mockserver.model.HttpResponse.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.charon.api.RpcDetails;
import gov.va.api.lighthouse.charon.api.RpcDetails.Parameter;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.api.v1.RpcRequestV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
public class LhsLighthouseRpcGatewayMocksV1 implements MockService {
  private final int port;

  private List<String> supportedQueries = new ArrayList<>();

  private List<Consumer<MockServerClient>> supportedRequests = List.of(this::respondByFile);

  private static LhsLighthouseRpcGatewayResponse.FilemanEntry asFilemanEntry(
      String file, String index) {
    return LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
        .file(file)
        .status("1")
        .ien(index)
        .index(index)
        .build();
  }

  @SneakyThrows
  private static HttpResponse chooseResponseBasedOnFile(HttpRequest request) {
    ObjectMapper mapper = JacksonConfig.createMapper();
    var rpcRequest = mapper.readValue(request.getBodyAsString(), RpcRequestV1.class);
    log.info("PROCESSING RPC REQUEST: {}", rpcRequest);
    Parameter ap = rpcRequest.rpc().parameters().get(0);
    String response;
    String body;
    if (ap.array().contains("api^create^coverage")) {
      response = writeResponse(ap);
      log.info("Responding with results: {}", response);
      body =
          json(
              RpcInvocationResultV1.builder()
                  .vista("673")
                  .timezone("America/New_York")
                  .response(response)
                  .build());
    } else {
      response = readResponse(ap);
      log.info("Responding with file content {}", response);
      body = rpcInvocationResultV1_OkWithContent(response);
    }

    return response().withStatusCode(200).withHeader(contentTypeApplicationJson()).withBody(body);
  }

  @SuppressWarnings("UnnecessaryParentheses")
  private static String readResponse(RpcDetails.Parameter ap) {
    // Looking for param^FILE^literal^2.312
    // File 2 only works here because the request is for fields of subfile .312
    return ap.array().stream()
        .filter(p -> p.startsWith("param^FILE^literal^") || "api^search^coverage".equals(p))
        .map(p -> p.replace("param^FILE^literal^", ""))
        .map(p -> p.replace("api^search^", ""))
        .map(
            matcher ->
                switch (matcher) {
                  case "36" -> "/lhslighthouserpcgateway/response-organization-read.json";
                  case "355.3" -> "/lhslighthouserpcgateway/response-insurance-plan-read.json";
                  case "355.33" -> "/lhslighthouserpcgateway"
                      + "/response-insurance-buffer-coverage-read.json";
                  case "365.12" -> "/lhslighthouserpcgateway"
                      + "/response-organization-payor-search.json";
                  default -> null;
                })
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No response for: " + ap));
  }

  @SuppressWarnings("UnnecessaryParentheses")
  private static String writeResponse(RpcDetails.Parameter ap) {
    var pattern = Pattern.compile("([0-9\\.]+)\\^([0-9]+)\\^#.*");
    var results =
        ap.array().stream()
            .filter(e -> pattern.matcher(e).matches())
            .map(
                entry -> {
                  String[] separated = entry.split("\\^", 4);
                  var file = separated[0];
                  var index = separated[1];
                  return asFilemanEntry(file, index);
                })
            .distinct()
            .collect(Collectors.toList());
    if (results.isEmpty()) {
      throw new IllegalStateException("Failed to build response for: " + ap);
    }
    var response = LhsLighthouseRpcGatewayResponse.Results.builder().results(results).build();
    return json(response);
  }

  void respondByFile(MockServerClient mock) {
    var details =
        RpcDetails.builder()
            .name(LhsLighthouseRpcGatewayGetsManifest.RPC_NAME)
            .context("LHS RPC CONTEXT")
            .build();
    supportedQueries.add(
        "[POST] http://localhost:" + port() + "/v1/rpc with RPC Details like " + json(details));
    mock.when(rpcQueryV1_WithExpectedRpcDetails(port(), details))
        .respond(LhsLighthouseRpcGatewayMocksV1::chooseResponseBasedOnFile);
  }
}
