package gov.va.api.health.vistafhirquery.service.controller.definitions;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class MappableIdentifierDefinition {
  @NonNull String vistaField;
  @NonNull String system;
  boolean isRequired;
}
