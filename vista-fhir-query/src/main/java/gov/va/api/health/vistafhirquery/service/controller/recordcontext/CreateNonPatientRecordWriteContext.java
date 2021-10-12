package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.unsetIdForCreate;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import lombok.Builder;

public class CreateNonPatientRecordWriteContext<BodyT extends IsResource>
    extends AbstractWriteContext<BodyT> implements CreateContext {

  /** Side effects: body.id will be unset. */
  @Builder
  public CreateNonPatientRecordWriteContext(String fileNumber, String site, BodyT body) {
    super(fileNumber, site, body);
    unsetIdForCreate(body);
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
