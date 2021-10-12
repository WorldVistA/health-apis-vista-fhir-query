package gov.va.api.health.vistafhirquery.service.controller.writes;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class CreatePatientRecordWriteContext<BodyT extends IsResource>
    extends AbstractWriteContext<BodyT> implements PatientRecordWriteContext<BodyT>, CreateContext {

  @Getter @NonNull private final String patientIcn;

  @Builder
  public CreatePatientRecordWriteContext(
      String fileNumber, String site, BodyT body, String patientIcn) {
    super(fileNumber, site, body);
    this.patientIcn = patientIcn;
  }

  @Override
  public CoverageWriteApi coverageWriteApi() {
    return CoverageWriteApi.CREATE;
  }

  @Override
  public String newResourceId() {
    return PatientTypeCoordinates.builder()
        .siteId(site())
        .icn(patientIcn())
        .recordId(result().ien())
        .build()
        .toString();
  }
}
