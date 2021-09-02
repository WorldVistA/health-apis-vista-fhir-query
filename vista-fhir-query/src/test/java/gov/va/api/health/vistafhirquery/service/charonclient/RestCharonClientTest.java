package gov.va.api.health.vistafhirquery.service.charonclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient.RequestFailed;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.lighthouse.charon.api.RpcDetails;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalLookupV1;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RestCharonClientTest {
  @Mock RestTemplate rt;
  @Mock RpcPrincipalLookupV1 lookup;

  RestCharonClient _charon() {
    return RestCharonClient.builder()
        .config(
            VistaApiConfig.builder()
                .url("http://charon.com/fugazi")
                .clientKey("ck")
                .lomaLindaHackContext("LL HACK")
                .vprGetPatientDataContext("VPR CTX")
                .build())
        .rpcPrincipalLookup(lookup)
        .restTemplate(rt)
        .build();
  }

  ResponseEntity<RpcInvocationResultV1> _httpResponse(int status) {
    return ResponseEntity.status(status).body(_result());
  }

  private Optional<RpcPrincipalV1> _knownPrincipal() {
    return Optional.of(
        RpcPrincipalV1.builder()
            .applicationProxyUser("API")
            .accessCode("ac")
            .verifyCode("vc")
            .build());
  }

  CharonRequest<FugaziRpc, FugaziResult> _request() {
    return CharonRequest.<FugaziRpc, FugaziResult>builder()
        .vista("123")
        .responseType(FugaziResult.class)
        .rpcRequest(new FugaziRpc())
        .build();
  }

  @SneakyThrows
  RpcInvocationResultV1 _result() {
    return RpcInvocationResultV1.builder()
        .vista("123")
        .timezone("America/New_York")
        .response(new ObjectMapper().writeValueAsString(new FugaziResult("foo")))
        .build();
  }

  @Test
  public void notFoundIsThrownForUnknownPrincipal() {
    when(lookup.findByNameAndSite(any(), any())).thenReturn(Optional.empty());
    assertThatExceptionOfType(NotFound.class).isThrownBy(() -> _charon().request(_request()));
  }

  @Test
  public void requestFailedIsThrownForNon200Responses() {
    when(lookup.findByNameAndSite(any(), any())).thenReturn(_knownPrincipal());
    when(rt.exchange(any(), eq(RpcInvocationResultV1.class))).thenReturn(_httpResponse(500));
    assertThatExceptionOfType(RequestFailed.class).isThrownBy(() -> _charon().request(_request()));
  }

  @Test
  public void responseIsReturned() {
    when(lookup.findByNameAndSite(any(), any())).thenReturn(_knownPrincipal());
    when(rt.exchange(any(), eq(RpcInvocationResultV1.class))).thenReturn(_httpResponse(200));
    var response = _charon().request(_request());
    var expected =
        CharonResponse.<FugaziRpc, FugaziResult>builder()
            .request(_request())
            .value(new FugaziResult("foo"))
            .invocationResult(_result())
            .build();
    assertThat(response).isEqualTo(expected);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  static class FugaziResult {
    @JsonProperty String fugazi;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  static class FugaziRpc implements TypeSafeRpcRequest {
    private String context = "FUGAZI CONTEXT";

    @Override
    public RpcDetails asDetails() {
      return RpcDetails.builder().name("FUGAZI RPC").context(context).build();
    }
  }
}
