package gov.va.api.health.vistafhirquery.interactivetests.organization;

import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class OrganizationReadTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void organizationRead() {
    TestContext ctx = new InteractiveTestContext("OrganizationRead");
    ctx.read(Organization.class, ctx.property("organizationId"));
  }
}
