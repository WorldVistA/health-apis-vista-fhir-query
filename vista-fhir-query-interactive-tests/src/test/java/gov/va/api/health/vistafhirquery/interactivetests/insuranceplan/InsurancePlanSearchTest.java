package gov.va.api.health.vistafhirquery.interactivetests.insuranceplan;

import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@Slf4j
public class InsurancePlanSearchTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void get() {
    TestContext ctx = new InteractiveTestContext("InsurancePlanSearch");
    ctx.search(InsurancePlan.class, Map.of("identifier", ctx.property("identifier")));
  }
}
