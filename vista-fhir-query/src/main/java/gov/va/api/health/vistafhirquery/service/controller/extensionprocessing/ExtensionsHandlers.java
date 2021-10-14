package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExtensionsHandlers {

  /** Safely get the value of an extension's codeable concept. */
  public static String extractCodeableConceptValueForSystem(
      CodeableConcept cc, String definingUrl, String system) {
    if (isBlank(cc)) {
      throw BadExtension.because(definingUrl, "extension.codeableConcept is null");
    }
    if (isBlank(cc.coding())) {
      throw BadExtension.because(definingUrl, "extension.codeableConept.coding is null");
    }
    var matchingCodings =
        cc.coding().stream().filter(c -> system.equals(c.system())).collect(toList());
    if (matchingCodings.size() > 1) {
      throw BadExtension.because(
          definingUrl, "Found multiple matching extension.codeableConcept.coding's for system");
    }
    if (isBlank(matchingCodings.get(0).code())) {
      throw BadExtension.because(definingUrl, " extension.codeableConcept.coding.code is null");
    }
    return matchingCodings.get(0).code();
  }
}
