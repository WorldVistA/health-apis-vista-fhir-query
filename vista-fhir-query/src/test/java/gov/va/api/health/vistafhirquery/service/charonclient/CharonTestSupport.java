package gov.va.api.health.vistafhirquery.service.charonclient;

import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;

import gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.CharonResponseAnswer.CharonResponseAnswerBuilder;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import lombok.Builder;
import lombok.Value;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CharonTestSupport {

  public static <I extends TypeSafeRpcRequest, O> CharonResponseAnswerBuilder<I, O> answerFor(
      ArgumentCaptor<CharonRequest<I, O>> captor) {
    return CharonResponseAnswer.<I, O>builder();
  }

  public static RpcInvocationResultV1 invocationResultV1(Object value) {
    return RpcInvocationResultV1.builder()
        .vista("123")
        .timezone("UTC")
        .response(json(value))
        .build();
  }

  @SuppressWarnings("unchecked")
  public static <I extends TypeSafeRpcRequest, R> ArgumentCaptor<CharonRequest<I, R>> requestCaptor(
      Class<I> requestType) {
    return ArgumentCaptor.forClass(CharonRequest.class);
  }

  @Value
  @Builder
  public static class CharonResponseAnswer<I extends TypeSafeRpcRequest, O>
      implements Answer<CharonResponse<I, O>> {

    RpcInvocationResultV1 invocationResult;
    O value;

    @Override
    public CharonResponse<I, O> answer(InvocationOnMock invocationOnMock) throws Throwable {
      CharonRequest<I, O> request = invocationOnMock.getArgument(0);
      return CharonResponse.<I, O>builder()
          .request(request)
          .invocationResult(invocationResult)
          .value(value)
          .build();
    }
  }
}
