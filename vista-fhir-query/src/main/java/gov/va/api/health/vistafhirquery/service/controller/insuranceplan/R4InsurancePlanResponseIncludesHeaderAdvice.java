package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.lighthouse.talos.ResponseIncludesIcnHeaderAdvice;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class R4InsurancePlanResponseIncludesHeaderAdvice implements ResponseBodyAdvice<Object> {

  @Delegate private final ResponseBodyAdvice<Object> delegate;

  R4InsurancePlanResponseIncludesHeaderAdvice() {
    delegate =
        ResponseIncludesIcnHeaderAdvice.<InsurancePlan, InsurancePlan.Bundle>builder()
            .type(InsurancePlan.class)
            .bundleType(InsurancePlan.Bundle.class)
            .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
            .extractIcns(body -> Stream.empty())
            .build();
  }
}
