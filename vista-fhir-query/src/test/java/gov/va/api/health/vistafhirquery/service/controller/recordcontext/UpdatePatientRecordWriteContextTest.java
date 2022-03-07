package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import org.junit.jupiter.api.Test;

public class UpdatePatientRecordWriteContextTest {

  private UpdatePatientRecordWriteContext<OperationOutcome> _context() {
    return UpdatePatientRecordWriteContext.<OperationOutcome>builder()
        .site("123")
        .fileNumber("8")
        .patientIcn("p1")
        .existingRecord(
            PatientTypeCoordinates.builder().icn("p1").file("8").site("123").ien("456").build())
        .existingRecordPublicId("p1^123^8^456")
        .body(OperationOutcome.builder().build())
        .build();
  }

  @Test
  void coverageWriteApi() {
    assertThat(_context().coverageWriteApi()).isEqualTo(CoverageWriteApi.UPDATE);
  }
}
