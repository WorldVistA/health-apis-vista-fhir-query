package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.r4.api.resources.Resource;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.net.URL;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class InteractiveTestClient implements TestClient {

  URL url;
  Resource body;

  @Override
  public void request() {
    RequestSpecification requestSpecification = RestAssured.given();
    // TODO: Add auth token header to request
    var response =
        requestSpecification.header("Content-Type", "application/json").body(body).post(url);
    log.info(response.body().prettyPrint());
  }
}
