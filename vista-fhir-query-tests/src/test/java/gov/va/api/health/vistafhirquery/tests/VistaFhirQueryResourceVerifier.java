package gov.va.api.health.vistafhirquery.tests;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VistaFhirQueryResourceVerifier {
  public static <T> Predicate<T> debugPrint() {
    return o -> {
      try {
        System.out.println("-".repeat(60));
        System.out.println(JacksonConfig.createMapper().writeValueAsString(o));
        System.out.println("-".repeat(60));
      } catch (Exception e) {
      }
      return true;
    };
  }

  public static TestIds ids() {
    return SystemDefinitions.systemDefinition().publicIds();
  }

  private static ResourceVerifier r4ForSite(String site) {
    return ResourceVerifier.builder()
        .apiPath(SystemDefinitions.systemDefinition().basePath().apiPath() + "hcs/" + site + "/r4/")
        .bundleClass(gov.va.api.health.r4.api.bundle.AbstractBundle.class)
        .testClient(TestClients.basePath())
        .operationOutcomeClass(gov.va.api.health.r4.api.resources.OperationOutcome.class)
        .maxCount(9999)
        .build();
  }

  public static ResourceVerifier r4ForSiteForTestPatient() {
    return r4ForSite(SystemDefinitions.systemDefinition().publicIds().siteForPatient());
  }

  public static ResourceVerifier r4WithoutSite() {
    return ResourceVerifier.builder()
        .apiPath(SystemDefinitions.systemDefinition().basePath().apiPath() + "r4/")
        .bundleClass(gov.va.api.health.r4.api.bundle.AbstractBundle.class)
        .testClient(TestClients.basePath())
        .operationOutcomeClass(gov.va.api.health.r4.api.resources.OperationOutcome.class)
        .maxCount(9999)
        .build();
  }
}
