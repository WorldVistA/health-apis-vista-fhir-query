package gov.va.api.health.vistafhirquery.interactivetests.insuranceplan;

import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class InsurancePlanReadTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void insurancePlanRead() {
    TestContext ctx = new InteractiveTestContext("InsurancePlanRead");
    ctx.read(InsurancePlan.class, ctx.property("insurancePlanId"));
  }
}
