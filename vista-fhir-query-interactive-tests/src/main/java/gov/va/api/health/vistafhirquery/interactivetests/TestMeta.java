package gov.va.api.health.vistafhirquery.interactivetests;

import io.restassured.http.Header;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestMeta {
  String requestUrl;
  Instant time;
  int httpStatus;
  String created;
  List<Header> responseHeaders;
}
