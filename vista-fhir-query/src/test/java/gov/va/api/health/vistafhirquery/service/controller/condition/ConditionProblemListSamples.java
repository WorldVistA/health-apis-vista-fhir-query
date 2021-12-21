package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Problems;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConditionProblemListSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {

    public static BundleLink link(BundleLink.LinkRelation rel, String base, String query) {
      return BundleLink.builder().relation(rel).url(base + "?" + query).build();
    }

    public Condition condition() {
      return condition("sNp1+673+PP;2931013.07;23");
    }

    public Condition condition(String id) {
      return Condition.builder()
          .id(id)
          .meta(Meta.builder().source("673").lastUpdated("2018-05-30T00:00:00Z").build())
          .clinicalStatus(
              CodeableConcept.builder()
                  .text("Active")
                  .coding(
                      Coding.builder()
                          .system("http://terminology.hl7.org/CodeSystem/condition-clinical")
                          .code("active")
                          .display("Active")
                          .build()
                          .asList())
                  .build())
          .category(
              CodeableConcept.builder()
                  .coding(
                      Coding.builder()
                          .code("problem-list-item")
                          .display("Problem List Item")
                          .system("http://terminology.hl7.org/CodeSystem/condition-category")
                          .build()
                          .asList())
                  .text("Problem List Item")
                  .build()
                  .asList())
          .code(
              CodeableConcept.builder()
                  .coding(
                      List.of(
                          Coding.builder()
                              .system("http://hl7.org/fhir/sid/icd-9-cm")
                              .code("401.9")
                              .display("UNSPECIFIED ESSENTIAL HYPERTENSION")
                              .build()))
                  .text("UNSPECIFIED ESSENTIAL HYPERTENSION")
                  .build())
          .subject(toReference("Patient", "p1", null))
          .verificationStatus(
              CodeableConcept.builder()
                  .coding(
                      Coding.builder()
                          .system("http://terminology.hl7.org/CodeSystem/condition-ver-status")
                          .code("confirmed")
                          .display("Confirmed")
                          .build()
                          .asList())
                  .text("Confirmed")
                  .build())
          .recordedDate("2018-05-30T00:00:00Z")
          .onsetDateTime("2010-01-06T00:00:00Z")
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class Vista {
    public Problems.Problem problem() {
      return problem("P;2931013.07;23");
    }

    public Problems.Problem problem(String id) {
      return Problems.Problem.builder()
          .id(ValueOnlyXmlAttribute.of(id))
          .codingSystem(ValueOnlyXmlAttribute.of("ICD"))
          .status(CodeAndNameXmlAttribute.of("A", "ACTIVE"))
          .icdd(ValueOnlyXmlAttribute.of("UNSPECIFIED ESSENTIAL HYPERTENSION"))
          .icd(ValueOnlyXmlAttribute.of("401.9"))
          .unverified(ValueOnlyXmlAttribute.of("0"))
          .onset(ValueOnlyXmlAttribute.of("3100106"))
          .entered(ValueOnlyXmlAttribute.of("3180530"))
          .updated(ValueOnlyXmlAttribute.of("3180530"))
          .build();
    }

    public List<Problems.Problem> problems() {
      return List.of(problem());
    }

    public VprGetPatientData.Response.Results results() {
      return results(problem());
    }

    public VprGetPatientData.Response.Results results(Problems.Problem problem) {
      return VprGetPatientData.Response.Results.builder()
          .version("1.13")
          .timeZone("-0500")
          .problems(Problems.builder().problemResults(List.of(problem)).build())
          .build();
    }
  }
}
