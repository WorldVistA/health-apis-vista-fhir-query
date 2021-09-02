package gov.va.api.health.vistafhirquery.tests;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VistaFhirQueryResourceVerifier {
  public static TestIds ids() {
    return SystemDefinitions.systemDefinition().publicIds();
  }

  public static ResourceVerifier r4ForSite(String site) {
    return ResourceVerifier.builder()
        .apiPath(
            SystemDefinitions.systemDefinition().basePath().apiPath() + "site/" + site + "/r4/")
        .bundleClass(gov.va.api.health.r4.api.bundle.AbstractBundle.class)
        .testClient(TestClients.basePath())
        .operationOutcomeClass(gov.va.api.health.r4.api.resources.OperationOutcome.class)
        .maxCount(9999)
        .build();
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
