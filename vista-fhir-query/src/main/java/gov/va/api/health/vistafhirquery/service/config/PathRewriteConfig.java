package gov.va.api.health.vistafhirquery.service.config;

import gov.va.api.lighthouse.talos.PathRewriteFilter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/** Configures the Talos path rewrite filter. */
@Slf4j
@Configuration
public class PathRewriteConfig {
  static String leadingPath() {
    return "/vista-fhir-query/";
  }

  @Bean
  FilterRegistrationBean<PathRewriteFilter> pathRewriteFilter() {
    var registration = new FilterRegistrationBean<PathRewriteFilter>();
    registration.setOrder(Ordered.LOWEST_PRECEDENCE);
    PathRewriteFilter filter =
        PathRewriteFilter.builder().removeLeadingPath(List.of(leadingPath())).build();
    registration.setFilter(filter);
    registration.addUrlPatterns(filter.removeLeadingPathsAsUrlPatterns());
    log.info("PathRewriteFilter enabled with priority {}", registration.getOrder());
    return registration;
  }
}
