package gov.va.api.health.vistafhirquery.service.controller.definitions;

import java.util.function.Function;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class MappableBooleanDefinition<FromT, ToT> {
  @NonNull String vistaField;
  @NonNull Function<FromT, Boolean> toBoolean;
  @NonNull Function<Boolean, ToT> fromBoolean;
}
