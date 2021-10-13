package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.CannotUpdateResourceWithMismatchedIds;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.MismatchedFileCoordinates;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Validations {
  public static <BodyT extends IsResource> void filesMatch(
      UpdateNonPatientRecordWriteContext<BodyT> ctx) {
    filesMatch(ctx.existingRecordPublicId(), ctx.existingRecord(), ctx.fileNumber());
  }

  /** Throw NotFound if the coordinates do not match an expected file. */
  public static void filesMatch(
      String publicId, RecordCoordinates requested, String... expectedFileNumbers) {
    for (var expectedFileNumber : expectedFileNumbers) {
      if (expectedFileNumber.equals(requested.file())) {
        return;
      }
    }
    throw MismatchedFileCoordinates.because(publicId, expectedFileNumbers, requested.file());
  }

  /** Throw CannotUpdateResourceWithMismatchedIds if body and existing record ids do not match. */
  public static <BodyT extends IsResource> void idsMatch(UpdateContext<BodyT> ctx) {
    var idFromBody = ctx.body().id();
    var idFromUrl = ctx.existingRecord().toString();
    if (isBlank(idFromBody) || !idFromUrl.equals(idFromBody)) {
      throw CannotUpdateResourceWithMismatchedIds.because(idFromUrl, idFromBody);
    }
  }

  /** Provide grouping of rules for non-patient record updates. */
  public static ContextValidationRule<UpdateNonPatientRecordWriteContext<?>>
      nonPatientRecordUpdateValidationRules() {
    return ctx -> {
      idsMatch(ctx);
      sitesMatch(ctx);
      filesMatch(ctx);
    };
  }

  /** Provide grouping of rules for patient record updates. */
  public static ContextValidationRule<UpdatePatientRecordWriteContext<?>>
      patientRecordUpdateValidationRules() {
    return ctx -> {
      idsMatch(ctx);
      sitesMatch(ctx);
      patientsMatch(ctx);
    };
  }

  /** Throw ExpectationFailed if patient and existing record patient do not match. */
  public static <BodyT extends IsResource> void patientsMatch(
      UpdatePatientRecordWriteContext<BodyT> ctx) {
    var icnFromBody = ctx.patientIcn();
    var icnFromId = ctx.existingRecord().icn();
    if (!icnFromBody.equals(icnFromId)) {
      throw ExpectationFailed.because(
          "Patient ICNs do not match: IcnFromBody(%s), IcnFromIdCoordinates(%s)",
          icnFromBody, icnFromId);
    }
  }

  /** Throw ExpectionedFailed if sites do not match. */
  public static <BodyT extends IsResource> void sitesMatch(UpdateContext<BodyT> ctx) {
    var siteFromUrl = ctx.site();
    var siteFromId = ctx.existingRecord().site();
    if (!siteFromId.equals(siteFromUrl)) {
      throw ExpectationFailed.because(
          "Site ids do not match: SiteFromIdCoordinates(%s), SiteFromUrl(%s)",
          siteFromId, siteFromUrl);
    }
  }
}
