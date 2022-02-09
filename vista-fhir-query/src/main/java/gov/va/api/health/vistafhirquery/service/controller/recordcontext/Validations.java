package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.IsSiteCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.MismatchedFileCoordinates;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Validations {
  public static <BodyT extends IsResource> void filesMatch(UpdateContext<BodyT> ctx) {
    filesMatch(ctx.existingRecordPublicId(), ctx.existingRecord(), ctx.fileNumber());
  }

  /** Throw NotFound if the coordinates do not match an expected file. */
  public static void filesMatch(
      String publicId, IsSiteCoordinates requested, String... expectedFileNumbers) {
    for (var expectedFileNumber : expectedFileNumbers) {
      if (expectedFileNumber.equals(requested.file())) {
        return;
      }
    }
    throw MismatchedFileCoordinates.because(publicId, expectedFileNumbers, requested.file());
  }
}
