package gov.va.api.health.vistafhirquery.interactivetests.organization;

import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.interactivetests.InteractiveTestContext;
import gov.va.api.health.vistafhirquery.interactivetests.TestContext;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class OrganizationSearchByTypeTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive-tests", matches = "true")
  void organizationSearchByType() {
    TestContext ctx = new InteractiveTestContext("OrganizationSearchByType");
    ctx.search(Organization.class, Map.of("type", ctx.property("type")));
  }
}
