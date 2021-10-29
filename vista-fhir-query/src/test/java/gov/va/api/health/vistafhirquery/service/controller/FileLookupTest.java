package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.vistafhirquery.service.charonclient.FilemanEntries;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import java.util.List;
import org.junit.jupiter.api.Test;

class FileLookupTest {

  @Test
  void findByFileNumberAndParentIen() {
    var f11_1_0 =
        FilemanEntry.builder().file("11").ien("1,1").status(FilemanEntries.SUCCESS).build();
    var f11_2_0 =
        FilemanEntry.builder().file("11").ien("1,2").status(FilemanEntries.SUCCESS).build();
    var f12_1_0 = FilemanEntry.builder().file("12").ien("2,1").status("nope").build();
    var f12_2_0 =
        FilemanEntry.builder().file("12").ien("2,2").status(FilemanEntries.SUCCESS).build();
    var results = Results.builder().results(List.of(f11_1_0, f11_2_0, f12_1_0, f12_2_0)).build();

    var l = FileLookup.of(results);
    assertThat(l.findByFileNumberAndParentIen("11", "2"))
        .describedAs("Finds successful entries")
        .containsExactlyInAnyOrder(f11_2_0);
    assertThat(l.findByFileNumberAndParentIen("12", "1"))
        .describedAs("Ignores failed entries")
        .isEmpty();
  }
}
