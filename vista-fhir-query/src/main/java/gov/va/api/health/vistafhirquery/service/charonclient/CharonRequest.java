package gov.va.api.health.vistafhirquery.service.charonclient;

import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@SuppressWarnings("ClassCanBeRecord")
@Value
@Builder
public class CharonRequest<I extends TypeSafeRpcRequest, O> {
  @NonNull String vista;
  @NonNull I rpcRequest;
  @NonNull Class<O> responseType;
}
