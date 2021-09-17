package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import gov.va.api.health.r4.api.resources.InsurancePlan;
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
public class R4InsurancePlanResponseIncludesIcnHeaderAdviceTest {
  @Mock R4SiteInsurancePlanController controller;

  private MockMvc mockMvc;

  @Test
  @SneakyThrows
  void insurancePlanResourceIsPatientAgnostic() {
    when(controller.insurancePlanRead("123", "o1"))
        .thenReturn(InsurancePlan.builder().id("123").build());
    mockMvc
        .perform(get("/site/123/r4/InsurancePlan/o1"))
        .andExpect(MockMvcResultMatchers.header().string("X-VA-INCLUDES-ICN", "NONE"));
  }

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new R4InsurancePlanResponseIncludesHeaderAdvice())
            .build();
  }
}
