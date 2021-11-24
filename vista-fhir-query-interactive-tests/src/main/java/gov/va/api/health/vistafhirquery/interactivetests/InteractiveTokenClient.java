package gov.va.api.health.vistafhirquery.interactivetests;

import static java.util.Arrays.asList;

import gov.va.api.lighthouse.testclients.clientcredentials.ClientCredentialsOauthClient;
import gov.va.api.lighthouse.testclients.clientcredentials.ClientCredentialsRequestConfiguration;
import lombok.Builder;

@Builder
public class InteractiveTokenClient implements TokenClient {

  InteractiveTestContext ctx;

  @Override
  public String clientCredentialsToken() {
    var config =
        ClientCredentialsRequestConfiguration.builder()
            .clientId(ctx.property("oauth.client-credentials.client-id"))
            .clientSecret(ctx.property("oauth.client-credentials.client-secret"))
            .audience(ctx.property("oauth.client-credentials.audience"))
            .scopes(asList(ctx.property("oauth.client-credentials.scopes").split(",")))
            .tokenUrl(ctx.property("oauth.client-credentials.token-url"))
            .build();
    var client = ClientCredentialsOauthClient.of(config);
    return client.requestToken().accessToken();
  }
}
