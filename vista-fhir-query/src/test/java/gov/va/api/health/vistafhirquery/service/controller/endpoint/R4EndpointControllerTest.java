package gov.va.api.health.vistafhirquery.service.controller.endpoint;

import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.vistafhirquery.service.controller.endpoint.EndpointSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class R4EndpointControllerTest {
  private R4EndpointController controller() {
    var bundlerFactory =
        R4BundlerFactory.builder()
            .linkProperties(
                LinkProperties.builder()
                    .defaultPageSize(15)
                    .maxPageSize(100)
                    .publicUrl("http://fake.com")
                    .publicR4BasePath("r4")
                    .build())
            .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
            .build();
    return new R4EndpointController(
        bundlerFactory, EndpointSamples.linkProperties(), EndpointSamples.rpcPrincipalLookup());
  }

  @Test
  void endpointSearch() {
    var request = requestFromUri("");
    var actual = controller().endpointSearch(request, null, 10);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(
                EndpointSamples.R4.create().endpoint("101"),
                EndpointSamples.R4.create().endpoint("103"),
                EndpointSamples.R4.create().endpoint("104")),
            3,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void endpointSearchCountThrowsException() {
    var request = requestFromUri("?_count=-1");
    assertThatExceptionOfType(ResourceExceptions.BadSearchParameters.class)
        .isThrownBy(() -> controller().endpointSearch(request, null, -1));
  }

  @Test
  void endpointSearchWithBadStatus() {
    var request = requestFromUri("?status=NONE");
    var actual = controller().endpointSearch(request, "NONE", 10);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(),
            0,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint?status=NONE"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @ParameterizedTest
  @ValueSource(strings = {"?status=active", "?status=active&_count=10"})
  void endpointSearchWithValidStatus(String query) {
    var request = requestFromUri(query);
    var actual = controller().endpointSearch(request, "active", 10);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(
                EndpointSamples.R4.create().endpoint("101"),
                EndpointSamples.R4.create().endpoint("103"),
                EndpointSamples.R4.create().endpoint("104")),
            3,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint" + query));
    assertThat(json(actual)).isEqualTo(json(expected));
  }
}
