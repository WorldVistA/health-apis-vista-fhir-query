package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.r4.api.resources.Appointment.AppointmentStatus;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Appointments;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class R4AppointmentTransformer {
  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull VprGetPatientData.Response.Results rpcResults;

  private AppointmentStatus appointmentStatus(ValueOnlyXmlAttribute status, String dateTime) {
    if (status == null || status.value() == null) {
      return null;
    }
    String appointmentStatus = status.value();
    switch (appointmentStatus) {
      case "NO-SHOW":
        return AppointmentStatus.noshow;
      case "CANCELLED":
        return AppointmentStatus.cancelled;
      case "SCHEDULED/KEPT":
        return bookedOrFulfilledStatus(dateTime);
      case "NO ACTION TAKEN":
        // Intentionally falling through NO ACTION TAKEN
      default:
        return AppointmentStatus.booked;
    }
  }

  CodeableConcept appointmentType(CodeAndNameXmlAttribute type) {
    if (isBlank(type)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            Coding.builder()
                .system("http://www.va.gov/Terminology/VistADefinedTerms/2.98-9.5")
                .display(type.name())
                .build()
                .asList())
        .text(type.name())
        .build();
  }

  AppointmentStatus bookedOrFulfilledStatus(String dateTime) {
    if (dateTime == null) {
      return AppointmentStatus.booked;
    }
    return Instant.parse(dateTime).isBefore(Instant.now())
        ? AppointmentStatus.fulfilled
        : AppointmentStatus.booked;
  }

  String idFrom(String id) {
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, VprGetPatientData.Domains.appointments, id);
  }

  List<Appointment.Participant> participants() {
    return Appointment.Participant.builder()
        .actor(toReference("Patient", patientIcn, null))
        .status(Appointment.ParticipationStatus.accepted)
        .build()
        .asList();
  }

  List<CodeableConcept> serviceCategory(ValueOnlyXmlAttribute serviceCategory) {
    if (isBlank(serviceCategory)) {
      return null;
    }
    String code = serviceCategoryCode(serviceCategory.value());
    if (code == null) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            Coding.builder()
                .system("http://www.va.gov/Terminology/VistADefinedTerms/44-9")
                .display(serviceCategory.value())
                .code(code)
                .build()
                .asList())
        .text(serviceCategory.value())
        .build()
        .asList();
  }

  String serviceCategoryCode(String display) {
    if (isBlank(display)) {
      return null;
    }
    switch (display) {
      case "MEDICINE":
        return "M";
      case "NEUROLOGY":
        return "N";
      case "NONE":
        return "0";
      case "PSYCHIATRY":
        return "P";
      case "REHAB MEDICINE":
        return "R";
      case "SURGERY":
        return "S";
      default:
        log.warn("Appointment service-category '{}' cannot be mapped to code", display);
        return null;
    }
  }

  List<CodeableConcept> serviceType(CodeAndNameXmlAttribute clinicStop) {
    if (isBlank(clinicStop)) {
      return null;
    }
    return CodeableConcept.builder().text(clinicStop.name()).build().asList();
  }

  private Appointment toAppointment(Appointments.Appointment rpcAppointment) {
    if (rpcAppointment == null) {
      return null;
    }
    String dateTime = toHumanDateTime(rpcAppointment.dateTime());
    return Appointment.builder()
        .id(idFrom(rpcAppointment.id().value()))
        .meta(Meta.builder().source(site).build())
        .status(appointmentStatus(rpcAppointment.apptStatus(), dateTime))
        .serviceCategory(serviceCategory(rpcAppointment.service()))
        .serviceType(serviceType(rpcAppointment.clinicStop()))
        .appointmentType(appointmentType(rpcAppointment.type()))
        .start(dateTime)
        .participant(participants())
        .build();
  }

  Stream<Appointment> toFhir() {
    return rpcResults.appointmentStream().map(this::toAppointment).filter(Objects::nonNull);
  }
}
