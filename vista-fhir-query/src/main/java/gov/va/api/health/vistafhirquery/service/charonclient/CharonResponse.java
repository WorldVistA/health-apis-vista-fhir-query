package gov.va.api.health.vistafhirquery.service.charonclient;

import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import java.time.ZoneId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CharonResponse<I extends TypeSafeRpcRequest, O> {
  @NonNull CharonRequest<I, O> request;
  @NonNull RpcInvocationResultV1 invocationResult;
  @NonNull O value;

  /** Convert the invocation result timezone to a ZoneId. */
  public ZoneId timezoneAsZoneId() {
    return ZoneId.of(invocationResult.timezone());
  }
}
