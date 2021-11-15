package gov.va.api.health.vistafhirquery.mockservices;

import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.contentTypeApplicationJson;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.json;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.rpcInvocationResultV1_OkWithContent;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.rpcQueryV1_WithExpectedRpcDetails;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.rpcQuery_WithExpectedRpcDetails;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.rpcResponse_OkWithContent;
import static gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Request.PatientId.forIcn;
import static org.mockserver.model.HttpResponse.response;

import gov.va.api.lighthouse.charon.api.RpcDetails;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.mockserver.client.MockServerClient;

/** Mocked responses for VPRGETPATIENTDATA. */
@Data
@RequiredArgsConstructor(staticName = "using")
public class VprGetPatientDataMocks implements MockService {
  private final int port;

  private List<String> supportedQueries = new ArrayList<>();

  private List<Consumer<MockServerClient>> supportedRequests =
      List.of(
          this::appointmentRead,
          this::appointmentSearch,
          this::appointmentSearchDate,
          this::observationReadLabs,
          this::observationReadVitals,
          this::observationSearch);

  private void addSupportedQuery(RpcDetails body) {
    supportedQueries.add(
        "[POST] http://localhost:" + port() + "/rpc with RPC Details like " + json(body));
  }

  void appointmentRead(MockServerClient mock) {
    var details =
        VprGetPatientData.Request.builder()
            .context(Optional.of("MOCKSERVICES"))
            .dfn(forIcn("1011537977V693883"))
            .type(Set.of(Domains.appointments))
            .id(Optional.of("A;2931013.07;23"))
            .build()
            .asDetails();

    supportedQueries.add(
        "[POST] http://localhost:" + port() + "/v1/rpc with RPC Details like " + json(details));
    mock.when(rpcQueryV1_WithExpectedRpcDetails(port(), details))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentTypeApplicationJson())
                .withBody(
                    rpcInvocationResultV1_OkWithContent(
                        "/vistalinkapi-vprgetpatientdata-appointment-readresponse.xml")));
  }

  void appointmentSearch(MockServerClient mock) {
    var details =
        VprGetPatientData.Request.builder()
            .context(Optional.of("MOCKSERVICES"))
            .dfn(forIcn("1011537977V693883"))
            .type(Set.of(Domains.appointments))
            // default lower bound
            .start(Optional.of("${local-fileman-date(1901-01-01T00:00:00Z)}"))
            // default upper bound
            .stop(Optional.of("${local-fileman-date(2700-01-01T00:00:00Z)}"))
            .build()
            .asDetails();

    supportedQueries.add(
        "[POST] http://localhost:" + port() + "/v1/rpc with RPC Details like " + json(details));
    mock.when(rpcQueryV1_WithExpectedRpcDetails(port(), details))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentTypeApplicationJson())
                .withBody(
                    rpcInvocationResultV1_OkWithContent(
                        "/vistalinkapi-vprgetpatientdata-appointment-searchresponse.xml")));
  }

  void appointmentSearchDate(MockServerClient mock) {
    var details =
        VprGetPatientData.Request.builder()
            .context(Optional.of("MOCKSERVICES"))
            .dfn(forIcn("1011537977V693883"))
            .type(Set.of(Domains.appointments))
            .start(Optional.of("${local-fileman-date(2010-01-01T00:00:00Z)}"))
            .stop(Optional.of("${local-fileman-date(2012-01-01T00:00:00Z)}"))
            .build()
            .asDetails();

    supportedQueries.add(
        "[POST] http://localhost:" + port() + "/v1/rpc with RPC Details like " + json(details));
    mock.when(rpcQueryV1_WithExpectedRpcDetails(port(), details))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentTypeApplicationJson())
                .withBody(
                    rpcInvocationResultV1_OkWithContent(
                        "/vistalinkapi-vprgetpatientdata-appointment-searchresponse.xml")));
  }

  void observationReadLabs(MockServerClient mock) {
    var body =
        VprGetPatientData.Request.builder()
            .context(Optional.of("MOCKSERVICES"))
            .dfn(forIcn("5000000347"))
            .type(Set.of(Domains.labs))
            .id(Optional.of("CH;6899892.91;14"))
            .build()
            .asDetails();
    addSupportedQuery(body);
    mock.when(rpcQuery_WithExpectedRpcDetails(port(), body))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentTypeApplicationJson())
                .withBody(
                    rpcResponse_OkWithContent(
                        "/vistalinkapi-vprgetpatientdata-read-labs-response.xml")));
  }

  void observationReadVitals(MockServerClient mock) {
    var body =
        VprGetPatientData.Request.builder()
            .context(Optional.of("MOCKSERVICES"))
            .dfn(forIcn("5000000347"))
            .type(Set.of(Domains.vitals))
            .id(Optional.of("32082"))
            .build()
            .asDetails();
    addSupportedQuery(body);
    mock.when(rpcQuery_WithExpectedRpcDetails(port(), body))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentTypeApplicationJson())
                .withBody(
                    rpcResponse_OkWithContent(
                        "/vistalinkapi-vprgetpatientdata-read-vitals-response.xml")));
  }

  void observationSearch(MockServerClient mock) {
    supportedQueries.add("[POST] http://localhost:" + port() + "/rpc with _any_ RPC Details");
    mock.when(rpcQuery_WithExpectedRpcDetails(port(), null))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentTypeApplicationJson())
                .withBody(
                    rpcResponse_OkWithContent(
                        "/vistalinkapi-vprgetpatientdata-observation-searchresponse.xml")));
  }
}
