package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CreateNonPatientRecordWriteContextTest {
  private CreateNonPatientRecordWriteContext<OperationOutcome> _context() {
    return CreateNonPatientRecordWriteContext.<OperationOutcome>builder()
        .site("123")
        .fileNumber("8")
        .body(OperationOutcome.builder().build())
        .build();
  }

  @Test
  void coverageWriteApi() {
    assertThat(_context().coverageWriteApi()).isEqualTo(CoverageWriteApi.CREATE);
  }

  @Test
  void newResourceId() {
    var ctx = _context();
    ctx.result(responseFor(List.of(FilemanEntry.builder().file("8").ien("1").status("1").build())));
    assertThat(ctx.newResourceId())
        .isEqualTo(RecordCoordinates.builder().site("123").file("8").ien("1").build().toString());
  }

  private LhsLighthouseRpcGatewayResponse responseFor(List<FilemanEntry> filemanEntries) {
    return LhsLighthouseRpcGatewayResponse.builder()
        .resultsByStation(Map.of("123", Results.builder().results(filemanEntries).build()))
        .build();
  }

  @Test
  void tooManyFilemanResultsThrows() {
    var ctx = _context();
    assertThatExceptionOfType(ExpectationFailed.class)
        .isThrownBy(
            () ->
                ctx.result(
                    responseFor(
                        List.of(
                            FilemanEntry.builder().file("8").ien("1").status("1").build(),
                            FilemanEntry.builder().file("8").ien("1").status("1").build()))));
  }
}
