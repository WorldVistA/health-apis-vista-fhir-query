package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.CoverageClass;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.ProtectedReferenceFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class R4CoverageWitnessProtectionAgentTest {

  @Mock LinkProperties linkProperties;

  @Test
  void referencesOfT() {
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
                List.of(
                    CoverageClass.builder().value("gp1").build(),
                    CoverageClass.builder().value("InsurancePlan/gp2").build(),
                    CoverageClass.builder().value("Prefix1/Prefix2/gp3").build()))
            .build();
    var wpa = new R4CoverageWitnessProtectionAgent(new ProtectedReferenceFactory(linkProperties));
    // By transforming to resource identity, we can test the advice gets all the references correct
    assertThat(
            wpa.referencesOf(coverage)
                .map(pr -> pr.asResourceIdentity().orElse(null))
                .collect(Collectors.toList()))
        .containsExactlyInAnyOrder(
            ResourceIdentity.builder().system("VISTA").resource("Patient").identifier("p1").build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Organization")
                .identifier("o1")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Organization")
                .identifier("o2")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("InsurancePlan")
                .identifier("gp1")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("InsurancePlan")
                .identifier("gp2")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("InsurancePlan")
                .identifier("gp3")
                .build(),
            ResourceIdentity.builder()
                .system("VISTA")
                .resource("Coverage")
                .identifier("cov1")
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
            .coverageClass(List.of(CoverageClass.builder().value("InsurancePlan/").build()))
            .build();
    var wpa = new R4CoverageWitnessProtectionAgent(new ProtectedReferenceFactory(linkProperties));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                wpa.referencesOf(coverage)
                    .map(pr -> pr.asResourceIdentity().orElse(null))
                    .collect(Collectors.toList()));
  }
}
