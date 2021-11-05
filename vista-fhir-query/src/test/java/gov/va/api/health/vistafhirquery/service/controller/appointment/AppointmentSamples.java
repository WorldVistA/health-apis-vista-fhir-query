package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Appointments;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppointmentSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    public static Appointment.Bundle asBundle(
        String baseUrl, Collection<Appointment> resources, int totalRecords, BundleLink... links) {
      return Appointment.Bundle.builder()
          .resourceType("Bundle")
          .total(totalRecords)
          .link(Arrays.asList(links))
          .type(AbstractBundle.BundleType.searchset)
          .entry(
              resources.stream()
                  .map(
                      resource ->
                          Appointment.Entry.builder()
                              .fullUrl(baseUrl + "/Appointment/" + resource.id())
                              .resource(resource)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(toList()))
          .build();
    }

    public static BundleLink link(BundleLink.LinkRelation rel, String base, String query) {
      return BundleLink.builder().relation(rel).url(base + "?" + query).build();
    }

    public Appointment appointment() {
      return appointment("sNp1+673+AA;2931013.07;23");
    }

    public Appointment appointment(String id) {
      return Appointment.builder()
          .id(id)
          .appointmentType(appointmentType())
          .meta(Meta.builder().source("673").build())
          .participant(
              Appointment.Participant.builder()
                  .actor(toReference("Patient", "p1", null))
                  .status(Appointment.ParticipationStatus.accepted)
                  .build()
                  .asList())
          .serviceCategory(serviceCategory())
          .serviceType(serviceType())
          .start("1993-10-13T07:00:00Z")
          .status(Appointment.AppointmentStatus.noshow)
          .build();
    }

    CodeableConcept appointmentType() {
      return CodeableConcept.builder()
          .coding(
              Coding.builder()
                  .system("http://www.va.gov/Terminology/VistADefinedTerms/2.98-9.5")
                  .display("REGULAR")
                  .build()
                  .asList())
          .text("REGULAR")
          .build();
    }

    List<CodeableConcept> serviceCategory() {
      return CodeableConcept.builder()
          .coding(
              Coding.builder()
                  .system("http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                  .display("MEDICINE")
                  .code("M")
                  .build()
                  .asList())
          .text("MEDICINE")
          .build()
          .asList();
    }

    List<CodeableConcept> serviceType() {
      return CodeableConcept.builder().text("GENERAL INTERNAL MEDICINE").build().asList();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class Vista {
    public Appointments.Appointment appointment() {
      return appointment("A;2931013.07;23");
    }

    public Appointments.Appointment appointment(String id) {
      return Appointments.Appointment.builder()
          .apptStatus(ValueOnlyXmlAttribute.of("NO-SHOW"))
          .clinicStop(CodeAndNameXmlAttribute.of("301", "GENERAL INTERNAL MEDICINE"))
          .dateTime(ValueOnlyXmlAttribute.of("2931013.07"))
          .facility(CodeAndNameXmlAttribute.of("673", "TAMPA (JAH VAH)"))
          .id(ValueOnlyXmlAttribute.of(id))
          .location(ValueOnlyXmlAttribute.of("GENERAL MEDICINE"))
          .patientClass(ValueOnlyXmlAttribute.of("AMB"))
          .provider(CodeAndNameXmlAttribute.of("1085", "MELDRUM,KEVIN"))
          .service(ValueOnlyXmlAttribute.of("MEDICINE"))
          .serviceCategory(CodeAndNameXmlAttribute.of("A", "AMBULATORY"))
          .type(CodeAndNameXmlAttribute.of("9", "REGULAR"))
          .visitString(ValueOnlyXmlAttribute.of("23;2931013.07;A"))
          .build();
    }

    public List<Appointments.Appointment> appointments() {
      return List.of(appointment());
    }

    public VprGetPatientData.Response.Results results() {
      return results(appointment());
    }

    public VprGetPatientData.Response.Results results(Appointments.Appointment appointment) {
      return VprGetPatientData.Response.Results.builder()
          .version("1.13")
          .timeZone("-0500")
          .appointments(Appointments.builder().appointmentResults(List.of(appointment)).build())
          .build();
    }
  }
}
