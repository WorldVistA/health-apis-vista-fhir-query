package gov.va.api.health.vistafhirquery.mockservices;

import static org.mockserver.model.HttpRequest.request;

import com.google.common.io.Resources;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.charon.api.RpcDetails;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.api.v1.RpcRequestV1;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;

/** MockServiceRequests. */
@Slf4j
@UtilityClass
public class MockServiceRequests {
  public static Header contentTypeApplicationJson() {
    return new Header("Content-Type", "application/json");
  }

  @SneakyThrows
  public static String fileContent(String resource) {
    return Resources.toString(
        MockServiceRequests.class.getResource(resource), StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public static String json(Object o) {
    return JacksonConfig.createMapper().writeValueAsString(o);
  }

  /** Return a json string representation of an RPC Response with an OK status. */
  public static String rpcInvocationResultV1_OkWithContent(String rpcResponseFile) {
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

  /** Create an HTTP Request for mocking a Vistalink API /v1/rpc endpoint. */
  public static HttpRequest rpcQueryV1_WithExpectedRpcDetails(int port, RpcDetails rpcDetails) {
    return rpcQueryWithPathAndRequest(
        port,
        "/v1/rpc",
        rpcDetails == null ? null : RpcRequestV1.builder().vista("673").rpc(rpcDetails).build());
  }

  @SneakyThrows
  private static HttpRequest rpcQueryWithPathAndRequest(int port, String path, Object rpcRequest) {
    log.info("Support Query [POST]: http://localhost:{}{}", port, path);
    URL url = new URL("http://localhost" + path);
    var request =
        request()
            .withMethod("POST")
            .withPath(url.getPath())
            .withHeader(contentTypeApplicationJson());
    if (rpcRequest != null) {
      String body = json(rpcRequest);
      log.info("With RPC Details like: {}", body);
      request =
          request.withBody(
              JsonBody.json(body, StandardCharsets.UTF_8, MatchType.ONLY_MATCHING_FIELDS));
    }
    return request;
  }
}
