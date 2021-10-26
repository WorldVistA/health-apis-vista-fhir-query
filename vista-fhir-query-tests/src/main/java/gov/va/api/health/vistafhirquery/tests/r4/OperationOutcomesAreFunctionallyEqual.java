package gov.va.api.health.vistafhirquery.tests.r4;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.ErrorsAreFunctionallyEqual;
import io.restassured.response.ResponseBody;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Helps tests determine if the Operation outcome is the same as what the one that is expected. */
@Slf4j
public class OperationOutcomesAreFunctionallyEqual implements ErrorsAreFunctionallyEqual {
  /** Remove fields unique to each instance: generated ID, timestamp, and encrypted data. */
  private OperationOutcome asOperationOutcomeWithoutDiagnostics(ResponseBody<?> body) {
    try {
      OperationOutcome oo = body.as(OperationOutcome.class);
      oo.id("REMOVED-FOR-COMPARISON");
      if (oo.extension() != null) {
        for (var ignoredExtension : List.of("troubleshooting", "message")) {
          oo.extension().stream()
              .filter(e -> e.url().equals(ignoredExtension))
              .forEach(e -> e.valueString("REMOVED-FOR-COMPARISON"));
        }
        oo.extension().stream()
            .filter(e -> e.url().equals("timestamp"))
            .forEach(e -> e.valueInstant("REMOVED-FOR-COMPARISON"));
      }
      return oo;
    } catch (Exception e) {
      log.error("Failed read response as OperationOutcome: {}", body.prettyPrint());
      throw e;
    }
  }

  @Override
  public boolean equals(ResponseBody<?> left, ResponseBody<?> right) {
    OperationOutcome ooLeft = asOperationOutcomeWithoutDiagnostics(left);
    OperationOutcome ooRight = asOperationOutcomeWithoutDiagnostics(right);
    boolean ok = ooLeft.equals(ooRight);
    if (!ok) {
      assertThat(ooLeft).usingRecursiveComparison().isEqualTo(ooRight);
    }
    return ok;
  }
}
