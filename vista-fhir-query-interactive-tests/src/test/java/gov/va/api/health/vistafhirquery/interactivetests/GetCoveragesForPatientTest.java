package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.r4.api.resources.Coverage;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@Slf4j
public class GetCoveragesForPatientTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void get() {
    /*
     * Do not copy me. I am a very bad pattern.
     */
    TestContext ctx = new InteractiveTestContext("GetCoveragesForPatient");
    var token = InteractiveTokenClient.builder().ctx(ctx).build().clientCredentialsToken();
    var url =
        StandardResourceUrls.builder()
            .baseUrl(ctx.property("baseurl"))
            .resourceName(Coverage.class.getSimpleName())
            .site(ctx.property("site"))
            .build()
            .search(Map.of("patient", ctx.property("patient")));
    RequestSpecification requestSpecification = RestAssured.given();
    var response =
        requestSpecification
            .log()
            .everything()
            .accept("application/json")
            .headers(Map.of("Authorization", "Bearer " + token))
            .get(url);
    response.prettyPrint();
    log.info("Response {}", response.statusCode());
  }
}
