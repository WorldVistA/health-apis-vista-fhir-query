package gov.va.api.health.vistafhirquery.tests;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.sentinel.ServiceDefinition;
import gov.va.api.lighthouse.testclients.clientcredentials.ClientCredentialsOauthClient;
import gov.va.api.lighthouse.testclients.clientcredentials.ClientCredentialsRequestConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

@Data
@Slf4j
public class CreateResourceVerifier {
  @NonNull private final ServiceDefinition serviceDefinition;

  @NonNull private final String requestPath;

  @NonNull private final IsResource requestBody;

  String accessToken;

  @Builder
  CreateResourceVerifier(
      @NonNull String apiName,
      @NonNull ServiceDefinition serviceDefinition,
      @NonNull String requestPath,
      @NonNull IsResource requestBody) {
    this.serviceDefinition = serviceDefinition;
    this.requestPath =
        requestPath.startsWith("/") && requestPath.length() > 1
            ? requestPath.substring(1)
            : requestPath;
    this.requestBody = requestBody;
    this.accessToken = getOauthToken(apiName);
  }

  private String createResourceAndGetLocation() {
    log.info(
        "Verify POST {} is (201) with Location header present.",
        serviceDefinition().apiPath() + requestPath());
    var response =
        RestAssured.given()
            .baseUri(serviceDefinition().url())
            .port(serviceDefinition().port())
            .relaxedHTTPSValidation()
            .contentType("application/json")
            .accept("application/json")
            .headers(Map.of("Authorization", "Bearer " + accessToken()))
            .body(requestBody())
            .request(Method.POST, serviceDefinition().apiPath() + requestPath());
    try {
      assertThat(response.getStatusCode()).isEqualTo(201);
      var location = response.getHeader(HttpHeaders.LOCATION);
      assertThat(location).isNotBlank();
      return location;
    } catch (AssertionError e) {
      log.info(
          "status: {}, location-header: {}, body: {}",
          response.getStatusCode(),
          response.getHeader(HttpHeaders.LOCATION),
          response.body().print());
      throw e;
    }
  }

  private IsResource getNewResourceById(String locationUrl) {
    assertThat(locationUrl).isNotBlank();
    log.info("Verify {} is {} status (200)", locationUrl, requestBody().getClass().getSimpleName());
    var response =
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(Map.of("Authorization", "Bearer " + accessToken()))
            .request(Method.GET, URI.create(locationUrl));
    try {
      var status = response.getStatusCode();
      assertThat(status).isEqualTo(200);
      return response.as(requestBody().getClass());
    } catch (AssertionError e) {
      log.info("status: {}, body: {}", response.getStatusCode(), response.body().print());
      throw e;
    }
  }

  private String getOauthToken(String apiName) {
    var tokenUrl =
        loadSystemPropertyOrEnvironmentVariable(apiName + ".client-credentials.token-url");
    var scopes =
        List.of(
            "system/" + requestBody().getClass().getSimpleName() + ".read",
            "system/" + requestBody().getClass().getSimpleName() + ".write");
    log.info("Requesting token from {} with scopes {}", tokenUrl, scopes);
    var oauthClient =
        ClientCredentialsOauthClient.builder()
            .config(
                ClientCredentialsRequestConfiguration.builder()
                    .clientId(
                        loadSystemPropertyOrEnvironmentVariable(
                            apiName + ".client-credentials.client-id"))
                    .clientSecret(
                        loadSystemPropertyOrEnvironmentVariable(
                            apiName + ".client-credentials.client-secret"))
                    .audience(
                        loadSystemPropertyOrEnvironmentVariable(
                            apiName + ".client-credentials.audience"))
                    .tokenUrl(tokenUrl)
                    .scopes(scopes)
                    .build())
            .build();
    var tokenResponse = oauthClient.requestToken();
    if (tokenResponse.isError()) {
      throw new IllegalStateException(
          "Failed to request client-credentials token:" + tokenResponse);
    }
    return tokenResponse.accessToken();
  }

  private String loadSystemPropertyOrEnvironmentVariable(String property) {
    if (isBlank(property)) {
      throw new IllegalArgumentException("Property name must not be blank.");
    }
    var value = System.getProperty(property);
    if (!isBlank(value)) {
      return value;
    }
    var envVar = property.toUpperCase().replaceAll("[^A-Z]", "_");
    value = System.getenv(envVar);
    return value;
  }

  public IsResource test() {
    var location = createResourceAndGetLocation();
    return getNewResourceById(location);
  }
}
