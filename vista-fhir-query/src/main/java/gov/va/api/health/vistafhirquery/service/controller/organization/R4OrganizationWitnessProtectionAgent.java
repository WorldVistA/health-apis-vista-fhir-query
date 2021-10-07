package gov.va.api.health.vistafhirquery.service.controller.organization;

import gov.va.api.health.r4.api.resources.Organization;
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

/** Creates protected references for the Organization resource. */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
public class R4OrganizationWitnessProtectionAgent implements WitnessProtectionAgent<Organization> {
  private final ProtectedReferenceFactory protectedReferenceFactory;

  private final HttpServletRequest request;

  @Override
  public Stream<ProtectedReference> referencesOf(Organization resource) {
    RequestPayloadModifier.forPayload(resource)
        .request(request)
        .addMeta(resource::meta)
        .build()
        .applyModifications();
    var referencesFromExtensions =
        resource.extension().stream()
            .filter(
                extension ->
                    OrganizationStructureDefinitions.VIA_INTERMEDIARY.equals(extension.url()))
            .map(
                extension ->
                    protectedReferenceFactory
                        .forReference(resource.meta().source(), extension.valueReference())
                        .orElse(null))
            .filter(Objects::nonNull);
    return Stream.concat(
        Stream.of(protectedReferenceFactory.forResource(resource, resource::id)),
        referencesFromExtensions);
  }
}
