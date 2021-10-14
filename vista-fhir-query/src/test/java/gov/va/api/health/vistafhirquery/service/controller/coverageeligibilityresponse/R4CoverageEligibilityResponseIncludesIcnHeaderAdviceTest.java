package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class R4CoverageEligibilityResponseIncludesIcnHeaderAdviceTest {
  @Mock R4SiteCoverageEligibilityResponseController controller;

  @Mock AlternatePatientIds alternatePatientIds;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(
                new R4CoverageEligibilityResponseIncludesIcnHeaderAdvice(alternatePatientIds))
            .build();
  }

  @Test
  @SneakyThrows
  public void subjectNotPopulated() {
    when(controller.coverageEligibilityResponseSearch(
            any(HttpServletRequest.class), eq("123"), eq("p1"), eq(15)))
        .thenReturn(CoverageEligibilityResponse.Bundle.builder().entry(List.of()).build());
    mockMvc
        .perform(get("/hcs/123/r4/CoverageEligibilityResponse?patient=p1&page=1&_count=15"))
        .andExpect(MockMvcResultMatchers.header().string("X-VA-INCLUDES-ICN", "NONE"));
  }

  @Test
  @SneakyThrows
  public void subjectPopulated() {
    when(controller.coverageEligibilityResponseSearch(
            any(HttpServletRequest.class), eq("123"), eq("p1"), eq(15)))
        .thenReturn(
            CoverageEligibilityResponse.Bundle.builder()
                .entry(
                    List.of(
                        CoverageEligibilityResponse.Entry.builder()
                            .resource(
                                CoverageEligibilityResponseSamples.R4
                                    .create()
                                    .coverageEligibilityResponse())
                            .build()))
                .build());
    when(alternatePatientIds.toPublicId(eq("p1"))).thenReturn("p1");
    mockMvc
        .perform(get("/hcs/123/r4/CoverageEligibilityResponse?patient=p1&page=1&_count=15"))
        .andExpect(MockMvcResultMatchers.header().string("X-VA-INCLUDES-ICN", "p1"));
  }

  @Test
  @SneakyThrows
  public void subjectPopulatedWithAlternateId() {
    when(controller.coverageEligibilityResponseSearch(
            any(HttpServletRequest.class), eq("123"), eq("p1"), eq(15)))
        .thenReturn(
            CoverageEligibilityResponse.Bundle.builder()
                .entry(
                    List.of(
                        CoverageEligibilityResponse.Entry.builder()
                            .resource(
                                CoverageEligibilityResponseSamples.R4
                                    .create()
                                    .coverageEligibilityResponse())
                            .build()))
                .build());
    when(alternatePatientIds.toPublicId(eq("p1"))).thenReturn("p99");
    mockMvc
        .perform(get("/hcs/123/r4/CoverageEligibilityResponse?patient=p1&page=1&_count=15"))
        .andExpect(MockMvcResultMatchers.header().string("X-VA-INCLUDES-ICN", "p99"));
  }
}
