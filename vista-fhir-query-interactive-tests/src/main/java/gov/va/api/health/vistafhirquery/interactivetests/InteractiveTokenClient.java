package gov.va.api.health.vistafhirquery.interactivetests;

import lombok.Builder;

@Builder
public class InteractiveTokenClient implements TokenClient {

  @Override
  public String clientCredentialsToken() {
    return null;
  }
}
