package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReference;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.RequestPayloadModifier;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtectionAgent;
import java.util.Objects;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Creates protected references for the Coverage resource. */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
public class R4CoverageWitnessProtectionAgent implements WitnessProtectionAgent<Coverage> {
  private final ProtectedReferenceFactory protectedReferenceFactory;

  private final HttpServletRequest request;

  @Override
  public Stream<ProtectedReference> referencesOf(Coverage resource) {
    RequestPayloadModifier.forPayload(resource)
        .request(request)
        .addMeta(resource::meta)
        .build()
        .applyModifications();

    Stream<ProtectedReference> referenceGroups =
        Stream.concat(
            Safe.stream(resource.payor())
                .map(
                    p ->
                        protectedReferenceFactory
                            .forReference(resource.meta().source(), p)
                            .orElse(null)),
            Safe.stream(resource.coverageClass())
                .map(
                    c -> {
                      int indexOfId = c.value().lastIndexOf('/') + 1;
                      if (c.value().length() <= indexOfId) {
                        throw new IllegalArgumentException(
                            "ID is malformed (appears to end with a /)");
                      }
                      return ProtectedReference.builder()
                          .type("InsurancePlan")
                          .id(c.value().substring(indexOfId))
                          .onUpdate(i -> c.value(c.value().substring(0, indexOfId) + i))
                          .build();
                    }));
    return Stream.concat(
            Stream.of(
                protectedReferenceFactory.forResource(resource, resource::id),
                protectedReferenceFactory
                    .forReferenceWithoutSite(resource.beneficiary())
                    .orElse(null)),
            referenceGroups)
        .filter(Objects::nonNull)
        .filter(reference -> isBlank(reference.id()) || !reference.id().startsWith("#"));
  }
}
