package gov.va.api.health.vistafhirquery.service.controller.endpoint;

import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.vistafhirquery.service.controller.endpoint.EndpointSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.health.vistafhirquery.service.mpifhirqueryclient.MpiFhirQueryClient;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class R4EndpointControllerTest {
  @Mock private MpiFhirQueryClient mpiFhirQueryClient;

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
        bundlerFactory,
        EndpointSamples.linkProperties(),
        EndpointSamples.rpcPrincipalLookupV1(),
        mpiFhirQueryClient);
  }

  @Test
  void endpointReadKnownSite() {
    var actual = controller().endpointRead("102");
    var expected = EndpointSamples.R4.create().endpoint("102");
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void endpointReadUnknownSiteIsNotFound() {
    assertThatExceptionOfType(ResourceExceptions.NotFound.class)
        .isThrownBy(() -> controller().endpointRead("987"));
  }

  @Test
  void endpointSearch() {
    var request = requestFromUri("");
    var actual = controller().endpointSearch(request, 10, null, null, null);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(
                EndpointSamples.R4.create().endpoint("101"),
                EndpointSamples.R4.create().endpoint("102"),
                EndpointSamples.R4.create().endpoint("103"),
                EndpointSamples.R4.create().endpoint("104")),
            4,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  @SneakyThrows
  void endpointSearchByPatient() {
    var request = requestFromUri("");
    when(mpiFhirQueryClient.stationIdsForPatient("1337"))
        .thenReturn(Set.of("101", "104", "105", "106"));
    var actual = controller().endpointSearch(request, 10, "1337", null, null);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(
                EndpointSamples.R4.create().endpoint("101"),
                EndpointSamples.R4.create().endpoint("104")),
            2,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  @SneakyThrows
  void endpointSearchByPatientAndStatus() {
    var request = requestFromUri("");
    when(mpiFhirQueryClient.stationIdsForPatient("1337")).thenReturn(Set.of("101", "105", "106"));
    var actual = controller().endpointSearch(request, 10, "1337", "active", null);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(EndpointSamples.R4.create().endpoint("101")),
            1,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void endpointSearchByPatientAndTag() {
    var request = requestFromUri("");
    when(mpiFhirQueryClient.stationIdsForPatient("1337")).thenReturn(Set.of("101", "105", "106"));
    var actual = controller().endpointSearch(request, 10, "1337", null, "example-v1");
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(EndpointSamples.R4.create().endpoint("101")),
            1,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  @SneakyThrows
  void endpointSearchByPatientAtUnknownSites() {
    var request = requestFromUri("");
    when(mpiFhirQueryClient.stationIdsForPatient("1337")).thenReturn(Set.of("222", "333", "444"));
    var actual = controller().endpointSearch(request, 10, "1337", "active", null);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(),
            0,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void endpointSearchByTag() {
    var request = requestFromUri("");
    var actual = controller().endpointSearch(request, 10, null, null, "example-v1");
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(
                EndpointSamples.R4.create().endpoint("101"),
                EndpointSamples.R4.create().endpoint("104")),
            2,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void endpointSearchCountThrowsException() {
    var request = requestFromUri("?_count=-1");
    assertThatExceptionOfType(ResourceExceptions.BadSearchParameters.class)
        .isThrownBy(() -> controller().endpointSearch(request, -1, null, null, null));
  }

  @Test
  void endpointSearchWithBadStatus() {
    var request = requestFromUri("?status=NONE");
    var actual = controller().endpointSearch(request, 10, null, "NONE", null);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(),
            0,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint?status=NONE"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void endpointSearchWithPatientAndBadStatus() {
    var request = requestFromUri("?status=NONE");
    var actual = controller().endpointSearch(request, 10, "1337", "NONE", null);
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
    var actual = controller().endpointSearch(request, 10, null, "active", null);
    var expected =
        EndpointSamples.R4.asBundle(
            "http://fake.com",
            List.of(
                EndpointSamples.R4.create().endpoint("101"),
                EndpointSamples.R4.create().endpoint("102"),
                EndpointSamples.R4.create().endpoint("103"),
                EndpointSamples.R4.create().endpoint("104")),
            4,
            link(BundleLink.LinkRelation.self, "http://fake.com/r4/Endpoint" + query));
    assertThat(json(actual)).isEqualTo(json(expected));
  }
}
