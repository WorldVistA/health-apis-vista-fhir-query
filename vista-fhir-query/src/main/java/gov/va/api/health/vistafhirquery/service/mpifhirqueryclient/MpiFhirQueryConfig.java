package gov.va.api.health.vistafhirquery.service.mpifhirqueryclient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Loads configuration of the VistaAPI. */
@Data
@Builder
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = false)
@EnableConfigurationProperties
@ConfigurationProperties("client.mpi-fhir-query")
public class MpiFhirQueryConfig {
  private String baseUrl;

  private String clientKey;
}
