package gov.va.api.health.vistafhirquery.mockservices;

import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.contentTypeApplicationJson;
import static gov.va.api.health.vistafhirquery.mockservices.MockServiceRequests.fileContent;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

/** Mock Requests/Results from MpiFhirQuery. */
@Data
@RequiredArgsConstructor(staticName = "using")
@Slf4j
public class MockMpiFhirQuery implements MockService {
  private final int port;

  private List<String> supportedQueries = new ArrayList<>();

  private List<Consumer<MockServerClient>> supportedRequests = List.of(this::respondByFile);

  private void addSupportedQuery(String patient) {
    supportedQueries.add("[GET] http://localhost:" + port() + "/r4/Endpoint?patient=" + patient);
  }

  /** Mock Patient request on given port. */
  @SneakyThrows
  public HttpRequest endpointPatientSearch(int port, String patient) {
    log.info("Support Query [GET]: http://localhost:{}", port);
    return request()
        .withMethod("GET")
        .withPath("/r4/Endpoint")
        .withQueryStringParameter("patient", patient);
  }

  private HttpResponse magicPatientResponse(HttpRequest request) {
    log.info("PROCESSING ENDPOINT PATIENT SEARCH: {}", request.getPath());
    return ok("/mfq/response-magic-patient-search.json");
  }

  /** Mock response as a string from file. */
  public String mfqResponseWithContent(String mfqResponseFile) {
    String response = fileContent(mfqResponseFile);
    log.info("Respond with: {}", response);
    return response;
  }

  private HttpResponse ok(String resultsFile) {
    log.info("Responding with {}", resultsFile);
    return response()
        .withStatusCode(200)
        .withHeader(contentTypeApplicationJson())
        .withBody(mfqResponseWithContent(resultsFile));
  }

  void respondByFile(MockServerClient mock) {
    var patient = "1011537977V693883";
    addSupportedQuery(patient);
    mock.when(endpointPatientSearch(port(), patient)).respond(this::magicPatientResponse);
  }
}
