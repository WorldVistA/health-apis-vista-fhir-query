package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

@FunctionalInterface
public interface ContextValidationRule<ContextT extends UpdateContext<?>> {
  void test(ContextT ctx);
}
