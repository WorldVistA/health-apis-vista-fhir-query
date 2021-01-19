package gov.va.api.health.vistafhirquery.service.config;

import static gov.va.api.lighthouse.talos.Responses.unauthorizedAsJson;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.lighthouse.talos.ClientKeyProtectedEndpointFilter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ClientKeyProtectedEndpointConfig {

  @Bean
  FilterRegistrationBean<ClientKeyProtectedEndpointFilter> clientKeyProtectedEndpointFilter(
      @Value("${vista-fhir-query.internal.client-keys}") String clientKeysCsv) {
    var registration = new FilterRegistrationBean<ClientKeyProtectedEndpointFilter>();

    List<String> clientKeys;

    if (isBlank(clientKeysCsv) || "disabled".equals(clientKeysCsv)) {
      log.warn(
          "Client-key protection is disabled. To enable, "
              + "set vista-fhir-query.internal.client-keys to a value other than disabled.");

      registration.setEnabled(false);
      clientKeys = List.of();
    } else {
      clientKeys = Arrays.stream(clientKeysCsv.split(",")).collect(Collectors.toList());
    }

    registration.setFilter(
        ClientKeyProtectedEndpointFilter.builder()
            .clientKeys(clientKeys)
            .name("Internal Vista-Fhir-Query Request")
            .unauthorizedResponse(unauthorizedResponse())
            .build());

    registration.addUrlPatterns("/internal/*");

    return registration;
  }

  @SneakyThrows
  private Consumer<HttpServletResponse> unauthorizedResponse() {
    return unauthorizedAsJson("{\"message\":\"Unauthorized: Check the client-key header.\"}");
  }
}
