package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import org.junit.jupiter.api.Test;

class UpdateNonPatientRecordWriteContextTest {

  private UpdateNonPatientRecordWriteContext<OperationOutcome> _context() {
    return UpdateNonPatientRecordWriteContext.<OperationOutcome>builder()
        .site("123")
        .fileNumber("8")
        .existingRecord(RecordCoordinates.builder().file("8").site("123").ien("456").build())
        .existingRecordPublicId("123^8^456")
        .body(OperationOutcome.builder().build())
        .build();
  }

  @Test
  void coverageWriteApi() {
    assertThat(_context().coverageWriteApi()).isEqualTo(CoverageWriteApi.UPDATE);
  }
}
