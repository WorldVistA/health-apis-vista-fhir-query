package gov.va.api.health.vistafhirquery.interactivetests.coverage;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class CoverageReadTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void coverageRead() {
    TestContext ctx = new InteractiveTestContext("CoverageRead");
    ctx.read(Coverage.builder().build(), ctx.property("coverageId"));
  }
}
