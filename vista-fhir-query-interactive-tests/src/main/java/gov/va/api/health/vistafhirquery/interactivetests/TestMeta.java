package gov.va.api.health.vistafhirquery.interactivetests;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestMeta {
  String requestUrl;
  Instant time;
  int httpStatus;
}
