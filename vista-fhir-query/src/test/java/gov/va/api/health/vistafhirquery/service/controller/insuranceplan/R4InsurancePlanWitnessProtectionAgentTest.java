package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.coverage.R4CoverageWitnessProtectionAgent;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;

class R4InsurancePlanWitnessProtectionAgentTest {

  @Mock LinkProperties linkProperties;
  MockHttpServletRequest request = new MockHttpServletRequest();

  @Test
  void referencesOfT() {
    var cer =
        InsurancePlan.builder()
            .id("ip1")
            .meta(Meta.builder().source("123").build())
            .ownedBy(Reference.builder().reference("Patient/123").build())
            .build();
    var wpa =
        new R4InsurancePlanWitnessProtectionAgent(
            new ProtectedReferenceFactory(linkProperties), mock(HttpServletRequest.class));
    // By transforming to resource identity, we can test the advice gets all the references correct
    assertThat(
            wpa.referencesOf(cer)
                .map(pr -> pr.asResourceIdentity().orElse(null))
                .collect(Collectors.toList()))
        .containsExactlyInAnyOrder(
            ResourceIdentity.builder()
                .system("VISTA")
                .identifier("ip1")
                .resource("InsurancePlan")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Patient")
                .identifier("123")
                .build());
  }

  @Test
  void referencesOfThrowsExceptionForBadId() {
    var coverage =
        Coverage.builder()
            .id("cov1")
            .meta(Meta.builder().source("123").build())
            .beneficiary(Reference.builder().reference("Patient/p1").build())
            .payor(
                List.of(
                    Reference.builder().reference("Organization/o1").build(),
                    Reference.builder().reference("Organization/o2").build()))
            .coverageClass(
                List.of(Coverage.CoverageClass.builder().value("InsurancePlan/").build()))
            .build();
    var wpa =
        new R4CoverageWitnessProtectionAgent(
            new ProtectedReferenceFactory(linkProperties), request);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                wpa.referencesOf(coverage)
                    .map(pr -> pr.asResourceIdentity().orElse(null))
                    .collect(Collectors.toList()));
  }
}
