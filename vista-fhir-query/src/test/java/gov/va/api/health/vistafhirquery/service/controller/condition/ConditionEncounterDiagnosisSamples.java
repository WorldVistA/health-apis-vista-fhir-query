package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Visits;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConditionEncounterDiagnosisSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    public static BundleLink link(BundleLink.LinkRelation rel, String base, String query) {
      return BundleLink.builder().relation(rel).url(base + "?" + query).build();
    }

    public Condition condition() {
      return condition("sNp1+123+TT;2931013.07;23:391.2");
    }

    public Condition condition(String id) {
      return Condition.builder()
          .id(id)
          .meta(Meta.builder().source("123").build())
          .category(
              CodeableConcept.builder()
                  .coding(
                      Coding.builder()
                          .code("encounter-diagnosis")
                          .display("Encounter Diagnosis")
                          .system("http://terminology.hl7.org/CodeSystem/condition-category")
                          .build()
                          .asList())
                  .text("Encounter Diagnosis")
                  .build()
                  .asList())
          .code(
              CodeableConcept.builder()
                  .coding(
                      List.of(
                          Coding.builder()
                              .system("http://hl7.org/fhir/sid/icd-9-cm")
                              .code("391.2")
                              .display("ACUTE RHEUMATIC MYOCARDITIS")
                              .build()))
                  .text("ACUTE RHEUMATIC MYOCARDITIS")
                  .build())
          .subject(toReference("Patient", "p1", null))
          .recordedDate("2002-04-16T21:20:42Z")
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class Vista {
    public VprGetPatientData.Response.Results results() {
      return results(visit());
    }

    public VprGetPatientData.Response.Results results(Visits.Visit visit) {
      return VprGetPatientData.Response.Results.builder()
          .version("1.13")
          .timeZone("-0500")
          .visits(Visits.builder().visitResults(List.of(visit)).build())
          .build();
    }

    public Visits.Visit visit() {
      return visit("T;2931013.07;23");
    }

    public Visits.Visit visit(String id) {
      return Visits.Visit.builder()
          .id(ValueOnlyXmlAttribute.of(id))
          .icd(
              List.of(
                  Visits.Icd.builder()
                      .code("391.2")
                      .name("ACUTE RHEUMATIC MYOCARDITIS")
                      .system("ICD")
                      .narrative("ACUTE RHEUMATIC MYOCARDITIS")
                      .ranking("P")
                      .build()))
          .dateTime(ValueOnlyXmlAttribute.of("3020416.212042"))
          .build();
    }

    public List<Visits.Visit> visits() {
      return List.of(visit());
    }
  }
}
