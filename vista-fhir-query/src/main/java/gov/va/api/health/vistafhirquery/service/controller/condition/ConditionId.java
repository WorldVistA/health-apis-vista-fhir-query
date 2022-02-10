package gov.va.api.health.vistafhirquery.service.controller.condition;

import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Condition ID processing class. */
@AllArgsConstructor
@Value
@Builder
public class ConditionId {
  SegmentedVistaIdentifier vistaId;

  Optional<String> icdCode;

  ConditionId(String privateId) {
    var colon = privateId.indexOf(':');
    if (colon == -1 || colon == privateId.length() - 1) {
      /*
       * Backwards compatibility to support early versions of IDs that might be in the wild.
       */
      this.vistaId = SegmentedVistaIdentifier.unpack(privateId);
      this.icdCode = Optional.empty();
    } else {
      var extractedVistaId = privateId.substring(0, colon);
      this.vistaId = SegmentedVistaIdentifier.unpack(extractedVistaId);
      this.icdCode = Optional.of(privateId.substring(colon + 1));
    }
  }

  public static ConditionId fromString(@NonNull String privateId) {
    return new ConditionId(privateId);
  }

  @Override
  public String toString() {
    return vistaId().pack() + (icdCode().isPresent() ? ":" + icdCode().get() : "");
  }
}
