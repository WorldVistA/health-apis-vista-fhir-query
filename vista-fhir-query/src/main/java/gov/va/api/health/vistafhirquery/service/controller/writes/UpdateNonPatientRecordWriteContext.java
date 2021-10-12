package gov.va.api.health.vistafhirquery.service.controller.writes;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class UpdateNonPatientRecordWriteContext<BodyT extends IsResource>
    extends AbstractWriteContext<BodyT> implements UpdateContext {

  @Getter @NonNull private final RecordCoordinates existingRecord;

  @Builder
  public UpdateNonPatientRecordWriteContext(
      String fileNumber, String site, BodyT body, RecordCoordinates existingRecord) {
    super(fileNumber, site, body);
    this.existingRecord = existingRecord;
  }

  @Override
  public CoverageWriteApi coverageWriteApi() {
    return CoverageWriteApi.UPDATE;
  }
}
