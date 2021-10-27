package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.referenceIdFromUri;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.talos.ResponseIncludesIcnHeaderAdvice;
import java.util.Objects;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type Appointment.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class R4AppointmentResponseIncludesIncHeaderAdvice implements ResponseBodyAdvice<Object> {
  @Delegate private final ResponseBodyAdvice<Object> delegate;

  R4AppointmentResponseIncludesIncHeaderAdvice(@Autowired AlternatePatientIds alternatePatientIds) {
    delegate =
        ResponseIncludesIcnHeaderAdvice.<Appointment, Appointment.Bundle>builder()
            .type(Appointment.class)
            .bundleType(Appointment.Bundle.class)
            .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
            .extractIcns(
                resource ->
                    Safe.stream(resource.participant())
                        .map(p -> p.actor())
                        .filter(Objects::nonNull)
                        .filter(actor -> StringUtils.contains(actor.reference(), "Patient"))
                        .map(actor -> referenceIdFromUri(actor).orElse(null))
                        .map(alternatePatientIds::toPublicId))
            .build();
  }
}
