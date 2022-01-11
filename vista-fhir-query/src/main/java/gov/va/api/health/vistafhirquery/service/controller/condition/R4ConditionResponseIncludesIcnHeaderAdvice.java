package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.referenceIdFromUri;

import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.talos.ResponseIncludesIcnHeaderAdvice;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type Condition.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class R4ConditionResponseIncludesIcnHeaderAdvice implements ResponseBodyAdvice<Object> {
  @Delegate private final ResponseBodyAdvice<Object> delegate;

  R4ConditionResponseIncludesIcnHeaderAdvice(@Autowired AlternatePatientIds alternatePatientIds) {
    delegate =
        ResponseIncludesIcnHeaderAdvice.<Condition, Condition.Bundle>builder()
            .type(Condition.class)
            .bundleType(Condition.Bundle.class)
            .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
            .extractIcns(
                resource ->
                    referenceIdFromUri(resource.subject())
                        .map(alternatePatientIds::toPublicId)
                        .stream())
            .build();
  }
}
