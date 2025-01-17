package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class UpdatePatientRecordWriteContext<BodyT extends IsResource>
    extends AbstractWriteContext<BodyT>
    implements PatientRecordWriteContext<BodyT>, UpdateContext<BodyT> {

  @Getter @NonNull private final String patientIcn;

  @Getter @NonNull private final PatientTypeCoordinates existingRecord;

  @Getter @NonNull private final String existingRecordPublicId;

  /** New instance. */
  @Builder
  public UpdatePatientRecordWriteContext(
      String fileNumber,
      String site,
      BodyT body,
      String patientIcn,
      String existingRecordPublicId,
      PatientTypeCoordinates existingRecord) {
    super(fileNumber, site, body);
    this.patientIcn = patientIcn;
    this.existingRecordPublicId = existingRecordPublicId;
    this.existingRecord = existingRecord;
  }

  @Override
  public CoverageWriteApi coverageWriteApi() {
    return CoverageWriteApi.UPDATE;
  }
}
