package gov.va.api.health.vistafhirquery.service.util;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Map.Entry;
import lombok.EqualsAndHashCode;

/** Basic implementation of a bi-directional map. */
@EqualsAndHashCode
public class BiMap<LEFT, RIGHT> {

  private final Map<LEFT, RIGHT> leftToRight;
  private final Map<RIGHT, LEFT> rightToLeft;

  /** Construct a new instance from left to right mappings. */
  public BiMap(Map<LEFT, RIGHT> leftToRight) {
    this.leftToRight = Map.copyOf(leftToRight);
    this.rightToLeft =
        leftToRight.entrySet().stream().collect(toMap(Entry::getValue, Entry::getKey));
  }

  public RIGHT leftToRight(LEFT left, RIGHT defaultRight) {
    return leftToRight.getOrDefault(left, defaultRight);
  }

  public LEFT rightToLeft(RIGHT right, LEFT defaultLeft) {
    return rightToLeft.getOrDefault(right, defaultLeft);
  }
}
