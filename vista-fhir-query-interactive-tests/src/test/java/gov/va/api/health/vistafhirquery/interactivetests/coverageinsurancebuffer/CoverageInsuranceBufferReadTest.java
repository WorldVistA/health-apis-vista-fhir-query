package gov.va.api.health.vistafhirquery.interactivetests.coverageinsurancebuffer;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class CoverageInsuranceBufferReadTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void coverageRead() {
    TestContext ctx = new InteractiveTestContext("CoverageInsuranceBufferRead");
    ctx.read(Coverage.class, ctx.property("coverageId"));
  }
}
