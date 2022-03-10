package gov.va.api.health.vistafhirquery.service.controller.definitions;

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
}
