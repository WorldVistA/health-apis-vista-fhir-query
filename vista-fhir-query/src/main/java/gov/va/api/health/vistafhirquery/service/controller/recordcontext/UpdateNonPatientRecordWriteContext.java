package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class UpdateNonPatientRecordWriteContext<BodyT extends IsResource>
    extends AbstractWriteContext<BodyT> implements UpdateContext<BodyT> {

  @Getter @NonNull private final RecordCoordinates existingRecord;

  @Getter @NonNull private final String existingRecordPublicId;

  /** Create a new instance. */
  @Builder
  public UpdateNonPatientRecordWriteContext(
      String fileNumber,
      String site,
      BodyT body,
      String existingRecordPublicId,
      RecordCoordinates existingRecord) {
    super(fileNumber, site, body);
    this.existingRecordPublicId = existingRecordPublicId;
    this.existingRecord = existingRecord;
  }

  @Override
  public CoverageWriteApi coverageWriteApi() {
    return CoverageWriteApi.UPDATE;
  }
}
