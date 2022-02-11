package gov.va.api.health.vistafhirquery.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "metadata.statement-type=patient",
      "spring.jpa.properties.hibernate.globally_quoted_identifiers=false",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.url=jdbc:h2:mem:db"
          + ";DB_CLOSE_DELAY=-1"
          + ";INIT=CREATE SCHEMA IF NOT EXISTS app;",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.datasource.initialization-mode=never",
      "spring.jpa.hibernate.ddl-auto=update",
      "vista-fhir-query.public-web-exception-key=set",
      "vista-fhir-query.rpc-principals.file=src/test/resources/principalsV1.json"
    })
public class PathRewriteConfigTest {
  @Autowired TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  @Test
  void pathIsRewritten() {
    assertThat(
        restTemplate.getForObject(
            "http://localhost:" + port + "/vista-fhir-query/actuator/health", String.class));
  }
}
