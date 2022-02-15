package gov.va.api.health.vistafhirquery.tests.r4;

import gov.va.api.health.r4.api.bundle.AbstractBundle;

public class R4TestSupport {
  /** Return true if the bundle exists. */
  public static <T extends AbstractBundle<?>> boolean isAnyBundle(T bundle) {
    return bundle != null;
  }

  /** Return true if the bundle has at least one entry. */
  public static <T extends AbstractBundle<?>> boolean isBundleWithAtLeastOneEntry(T bundle) {
    return !bundle.entry().isEmpty();
  }
}
