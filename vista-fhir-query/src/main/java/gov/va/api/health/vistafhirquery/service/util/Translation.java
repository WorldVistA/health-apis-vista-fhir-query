package gov.va.api.health.vistafhirquery.service.util;

import java.util.Optional;

public interface Translation<FromT, ToT> {
  ToT lookup(FromT from);

  default Optional<ToT> translate(FromT from) {
    return Optional.ofNullable(lookup(from));
  }
}
