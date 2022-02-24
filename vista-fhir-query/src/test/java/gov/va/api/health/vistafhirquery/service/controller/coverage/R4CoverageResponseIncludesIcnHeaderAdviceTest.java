package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
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
public class R4CoverageResponseIncludesIcnHeaderAdviceTest {
  @Mock R4SiteInsuranceBufferCoverageController controller;

  @Mock AlternatePatientIds alternatePatientIds;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new R4CoverageResponseIncludesIcnHeaderAdvice(alternatePatientIds))
            .build();
  }

  @Test
  @SneakyThrows
  public void subjectNotPopulated() {
    when(controller.coverageRead("123", "456")).thenReturn(Coverage.builder().build());
    mockMvc
        .perform(get("/hcs/123/r4/Coverage/456"))
        .andExpect(MockMvcResultMatchers.header().string("X-VA-INCLUDES-ICN", "NONE"));
  }

  @Test
  @SneakyThrows
  public void subjectPopulated() {
    when(controller.coverageRead("123", "456"))
        .thenReturn(CoverageSamples.R4.create().coverage("666", "1,8,", "p1"));
    when(alternatePatientIds.toPublicId(eq("p1"))).thenReturn("p1");
    mockMvc
        .perform(get("/hcs/123/r4/Coverage/456"))
        .andExpect(MockMvcResultMatchers.header().string("X-VA-INCLUDES-ICN", "p1"));
  }

  @Test
  @SneakyThrows
  public void subjectPopulatedWithAlternateId() {
    when(controller.coverageRead("123", "456"))
        .thenReturn(CoverageSamples.R4.create().coverage("666", "1,8,", "p1"));
    when(alternatePatientIds.toPublicId(eq("p1"))).thenReturn("p99");
    mockMvc
        .perform(get("/hcs/123/r4/Coverage/456"))
        .andExpect(MockMvcResultMatchers.header().string("X-VA-INCLUDES-ICN", "p99"));
  }
}
