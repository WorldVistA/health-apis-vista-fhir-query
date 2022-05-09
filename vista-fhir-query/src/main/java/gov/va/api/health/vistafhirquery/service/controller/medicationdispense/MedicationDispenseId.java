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
    /*
     * colon is not compliant with FHIR ID characters when using clear IDs. We'll retain parsing it
     * for backwards compatibility.
     *
     * This class is dumb. I hate it very much.
     */
    var whyIsThisNotJustPartOfSegmentIdDelimiter = privateId.lastIndexOf(':');
    if (whyIsThisNotJustPartOfSegmentIdDelimiter == -1) {
      whyIsThisNotJustPartOfSegmentIdDelimiter = privateId.lastIndexOf('.');
    }
    if (whyIsThisNotJustPartOfSegmentIdDelimiter == -1
        || whyIsThisNotJustPartOfSegmentIdDelimiter == privateId.length() - 1) {
      throw new MalformedId("Missing fill date: " + privateId);
    }
    var extractedVistaId = privateId.substring(0, whyIsThisNotJustPartOfSegmentIdDelimiter);
    this.vistaId = SegmentedVistaIdentifier.unpack(extractedVistaId);
    this.fillDate = privateId.substring(whyIsThisNotJustPartOfSegmentIdDelimiter + 1);
  }

  public static MedicationDispenseId fromString(@NonNull String privateId) {
    return new MedicationDispenseId(privateId);
  }

  @Override
  public String toString() {
    return vistaId().pack() + "." + fillDate();
  }

  public static class MalformedId extends RuntimeException {
    MalformedId(String message) {
      super(message);
    }
  }
}
