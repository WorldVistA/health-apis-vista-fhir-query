package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** MedicationDispense ID processing class. */
@Value
@AllArgsConstructor
@Builder
public class MedicationDispenseId {
  @NonNull SegmentedVistaIdentifier vistaId;
  @NonNull String fillDate;

  private MedicationDispenseId(String privateId) {
    var colon = privateId.lastIndexOf(':');
    if (colon == -1 || colon == privateId.length() - 1) {
      throw new MalformedId("Missing fill date: " + privateId);
    }
    var extractedVistaId = privateId.substring(0, colon);
    this.vistaId = SegmentedVistaIdentifier.unpack(extractedVistaId);
    this.fillDate = privateId.substring(colon + 1);
  }

  public static MedicationDispenseId fromString(@NonNull String privateId) {
    return new MedicationDispenseId(privateId);
  }

  @Override
  public String toString() {
    return vistaId().pack() + ":" + fillDate();
  }

  public static class MalformedId extends RuntimeException {
    MalformedId(String message) {
      super(message);
    }
  }
}
