package gov.va.api.health.vistafhirquery.service.controller.definitions;

import java.util.function.Function;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class MappableCodeDefinition<FromT, ToT> {
  @NonNull String vistaField;
  @NonNull Function<String, ToT> fromCode;
  @NonNull Function<FromT, String> toCode;
  boolean isRequired;
}
