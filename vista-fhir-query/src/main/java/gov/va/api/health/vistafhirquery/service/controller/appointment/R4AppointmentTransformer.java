package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.patientCoordinateStringFrom;

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

  private Appointment toAppointmentSkeleton(Appointments.Appointment rpcAppointment) {
    return Appointment.builder()
        .id(patientCoordinateStringFrom(patientIcn, site, rpcAppointment.id().value()))
        .meta(Meta.builder().source(site).build())
        .participant(
            Appointment.Participant.builder()
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
