package gov.va.api.health.vistafhirquery.service.charonclient;

import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.api.v1.RpcRequestV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;

public interface CharonClient {
  RpcInvocationResultV1 makeRequest(RpcRequestV1 rpcRequest);

  <I extends TypeSafeRpcRequest, O> CharonResponse<I, O> request(CharonRequest<I, O> request);

  class RequestFailed extends RuntimeException {
    public RequestFailed(String message) {
      super(message);
    }
  }
}
