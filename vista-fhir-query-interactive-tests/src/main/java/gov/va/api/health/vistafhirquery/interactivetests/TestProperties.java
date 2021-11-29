package gov.va.api.health.vistafhirquery.interactivetests;

public interface TestProperties {
  String property(String name);

  class PropertyNotDefined extends RuntimeException {

    public PropertyNotDefined(String property) {
      super(String.format("Property '%s' not defined", property));
    }
  }
}
