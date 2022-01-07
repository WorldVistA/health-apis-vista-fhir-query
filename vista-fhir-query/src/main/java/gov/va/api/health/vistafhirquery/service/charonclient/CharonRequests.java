package gov.va.api.health.vistafhirquery.service.charonclient;

import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CharonRequests {
  /** Create new request that has an LhsLighthouseRpcGatewayResponse. */
  public static <I extends TypeSafeRpcRequest>
      CharonRequest<I, LhsLighthouseRpcGatewayResponse.Results> lighthouseRpcGatewayRequest(
          @NonNull String vista, @NonNull I request) {
    return CharonRequest.<I, LhsLighthouseRpcGatewayResponse.Results>builder()
        .vista(vista)
        .rpcRequest(request)
        .responseType(LhsLighthouseRpcGatewayResponse.Results.class)
        .build();
  }

  /** Create a gateway response of exactly one result. */
  public static LhsLighthouseRpcGatewayResponse lighthouseRpcGatewayResponse(
      CharonResponse<?, LhsLighthouseRpcGatewayResponse.Results> response) {
    return LhsLighthouseRpcGatewayResponse.builder()
        .resultsByStation(Map.of(response.request().vista(), response.value()))
        .build();
  }

  /** Create new request that has a VPRGetPatientData. */
  public static <I extends TypeSafeRpcRequest>
      CharonRequest<I, VprGetPatientData.Response.Results> vprGetPatientData(
          @NonNull String vista, @NonNull I request) {
    return CharonRequest.<I, VprGetPatientData.Response.Results>builder()
        .vista(vista)
        .rpcRequest(request)
        .responseType(VprGetPatientData.Response.Results.class)
        .build();
  }

  /** Create a gateway response of exactly one result. */
  public static VprGetPatientData.Response vprGetPatientDataResponse(
      CharonResponse<VprGetPatientData.Request, VprGetPatientData.Response.Results> response) {
    return VprGetPatientData.Response.builder()
        .resultsByStation(Map.of(response.request().vista(), response.value()))
        .build();
  }
}
