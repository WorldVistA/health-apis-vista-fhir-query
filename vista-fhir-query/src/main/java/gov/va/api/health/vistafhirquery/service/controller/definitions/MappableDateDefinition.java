package gov.va.api.health.vistafhirquery.service.controller.definitions;

import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class MappableDateDefinition {
  @NonNull String vistaField;
  boolean isRequired;
  @NonNull DateTimeFormatter vistaDateFormatter;
}
