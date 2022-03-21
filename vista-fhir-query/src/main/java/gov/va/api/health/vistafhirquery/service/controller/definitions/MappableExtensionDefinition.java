package gov.va.api.health.vistafhirquery.service.controller.definitions;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.elements.Extension;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class MappableExtensionDefinition<DefT> {
  @NonNull String structureDefinition;
  @NonNull DefT valueDefinition;

  public static <D> MappableExtensionDefinitionBuilder<D> forValueDefinition(D valueDefinition) {
    return MappableExtensionDefinition.<D>builder().valueDefinition(valueDefinition);
  }

  public boolean isInCollection(Collection<Extension> extensions) {
    return Safe.stream(extensions)
        .anyMatch(extension -> structureDefinition().equals(extension.url()));
  }
}
