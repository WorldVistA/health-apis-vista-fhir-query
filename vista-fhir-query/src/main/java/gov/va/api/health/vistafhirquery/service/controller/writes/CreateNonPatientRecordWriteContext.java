package gov.va.api.health.vistafhirquery.service.controller.writes;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import lombok.Builder;

public class CreateNonPatientRecordWriteContext<BodyT extends IsResource>
    extends AbstractWriteContext<BodyT> implements CreateContext {

  @Builder
  public CreateNonPatientRecordWriteContext(String fileNumber, String site, BodyT body) {
    super(fileNumber, site, body);
  }

  @Override
  public CoverageWriteApi coverageWriteApi() {
    return CoverageWriteApi.CREATE;
  }

  @Override
  public String newResourceId() {
    return RecordCoordinates.builder()
        .site(site())
        .ien(result().ien())
        .file(fileNumber())
        .build()
        .toString();
  }
}
