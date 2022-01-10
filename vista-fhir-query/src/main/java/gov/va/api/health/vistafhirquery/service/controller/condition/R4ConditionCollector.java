package gov.va.api.health.vistafhirquery.service.controller.condition;

import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Problems;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Collects Conditions, problems and visits. */
@Value
@Builder
public class R4ConditionCollector {
  private final String patientIcn;

  @NonNull private final VprGetPatientData.Response.Results results;

  @NonNull String site;

  Stream<Condition> toFhir() {
    Stream<Condition> problems =
        results
            .problemStream()
            .filter(Problems.Problem::isNotEmpty)
            .flatMap(
                problem ->
                    VistaProblemToR4ConditionTransformer.builder()
                        .patientIcn(patientIcn)
                        .site(site)
                        .vistaProblem(problem)
                        .build()
                        .toFhir());
    return problems;
  }
}
