package gov.va.api.health.vistafhirquery.service.charonclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalLookupV1;
import gov.va.api.lighthouse.charon.api.v1.RpcRequestV1;
import gov.va.api.lighthouse.charon.models.RpcModelExceptions;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
public class RestCharonClient implements CharonClient {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final ObjectMapper XML_MAPPER =
      new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  RestTemplate restTemplate;

  VistaApiConfig config;

  RpcPrincipalLookupV1 rpcPrincipalLookup;

  /** Determine which type of input data to process. */
  @SneakyThrows
  static <T> T deserialize(String input, Class<T> type) {
    if (StringUtils.isEmpty(input)) {
      throw new RpcModelExceptions.InvalidRpcResponse("Vista response is null");
    }
    if (input.charAt(0) == '<') {
      return fromXml(input, type);
    } else {
      return fromJson(input, type);
    }
  }

  /** Quietly unmarshal the JSON to the object type. */
  @SneakyThrows
  static <T> T fromJson(String json, Class<T> type) {
    try {
      return MAPPER.readValue(json, type);
    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize JSON to {}: {}", type.getName(), e.getMessage());
      throw e;
    }
  }

  /** Quietly unmarshal the XML to the object type. */
  @SneakyThrows
  static <T> T fromXml(String xml, Class<T> type) {
    try {
      return XML_MAPPER.readValue(xml, type);
    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize XML to {}: {}", type.getName(), e.getMessage());
      throw e;
    }
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

  @SneakyThrows
  private <I extends TypeSafeRpcRequest, O> void debugRequest(CharonRequest<I, O> request) {
    if (!config().isDebugCharon()) {
      return;
    }
    log.info(
        "REQUEST\n{}",
        new ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(request.rpcRequest().asDetails()));
  }

  private <I extends TypeSafeRpcRequest, O> void debugResponse(CharonResponse<I, O> response) {
    if (!config().isDebugCharon()) {
      return;
    }
    String pretty;
    try {
      pretty = new ObjectMapper().readTree(response.invocationResult().response()).toPrettyString();
    } catch (JsonProcessingException e) {
      pretty = response.invocationResult().response();
    }
    log.info("RESPONSE\n{}", pretty);
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
    debugRequest(request);
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

    var response =
        CharonResponse.<I, O>builder()
            .request(request)
            .invocationResult(invocationResult)
            .value(deserialize(invocationResult.response(), request.responseType()))
            .build();
    debugResponse(response);
    return response;
  }

  private void verifyVistalinkApiResponse(ResponseEntity<RpcInvocationResultV1> response) {
    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RequestFailed(String.format("Status %s", response.getStatusCode()));
    }
  }
}
