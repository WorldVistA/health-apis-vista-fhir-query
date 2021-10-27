package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;

import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Appointments;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4AppointmentTransformer {
  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull VprGetPatientData.Response.Results rpcResults;

  String idFrom(String id) {
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, VprGetPatientData.Domains.appointments, id);
  }

  private Appointment toAppointmentSkeleton(Appointments.Appointment rpcAppointment) {
    return Appointment.builder()
        .id(idFrom(rpcAppointment.id().value()))
        .meta(Meta.builder().source(site).build())
        .participant(
            Appointment.Participant.builder()
                .actor(toReference("Patient", patientIcn, null))
                .status(Appointment.ParticipationStatus.accepted)
                .build()
                .asList())
        .status(Appointment.AppointmentStatus.proposed)
        .build();
  }

  Stream<Appointment> toFhirSkeleton() {
    return rpcResults
        .appointmentStream()
        .filter(Objects::nonNull)
        .map(this::toAppointmentSkeleton)
        .filter(Objects::nonNull);
  }
}
