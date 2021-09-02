package gov.va.api.health.vistafhirquery.service.charonclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalLookupV1;
import gov.va.api.lighthouse.charon.api.v1.RpcRequestV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Value
@Builder
@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class RestCharonClient implements CharonClient {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  RestTemplate restTemplate;

  VistaApiConfig config;

  RpcPrincipalLookupV1 rpcPrincipalLookup;

  /** Quietly unmarshal the JSON to the object type. */
  @SneakyThrows
  static <T> T fromJson(String json, Class<T> type) {
    return MAPPER.readValue(json, type);
  }

  @SneakyThrows
  private RequestEntity<RpcRequestV1> buildRequestEntity(RpcRequestV1 body) {
    var baseUrl = config().getUrl();
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    return RequestEntity.post(new URI(baseUrl + "/v1/rpc"))
        .contentType(MediaType.APPLICATION_JSON)
        .header("client-key", config().getClientKey())
        .body(body);
  }

  /** Make a request using a full RPC Request. */
  @Override
  @SneakyThrows
  public RpcInvocationResultV1 makeRequest(RpcRequestV1 rpcRequest) {
    var request = buildRequestEntity(rpcRequest);
    var response = restTemplate.exchange(request, RpcInvocationResultV1.class);
    verifyVistalinkApiResponse(response);
    return response.getBody();
  }

  @Override
  public <I extends TypeSafeRpcRequest, O> CharonResponse<I, O> request(
      @NonNull CharonRequest<I, O> request) {
    var details = request.rpcRequest().asDetails();
    var principal =
        rpcPrincipalLookup
            .findByNameAndSite(details.name(), request.vista())
            .orElseThrow(() -> new ResourceExceptions.NotFound("Site " + request.vista()));
    var invocationResult =
        makeRequest(
            RpcRequestV1.builder()
                .vista(request.vista())
                .principal(principal)
                .rpc(details)
                .build());
    return CharonResponse.<I, O>builder()
        .request(request)
        .invocationResult(invocationResult)
        .value(fromJson(invocationResult.response(), request.responseType()))
        .build();
  }

  private void verifyVistalinkApiResponse(ResponseEntity<RpcInvocationResultV1> response) {
    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RequestFailed(String.format("Status %s", response.getStatusCode()));
    }
  }
}
