package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.ids.client.IdEncoder.BadId;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import org.junit.jupiter.api.Test;

class WitnessProtectionTest {

  @Test
  void toPatientTypeCoordinates() {
    assertThat(
            new FugaziWP()
                .toPatientTypeCoordinatesOrDie("fugazi:123+456+789", Patient.class, "888"))
        .isEqualTo(PatientTypeCoordinates.fromString("123+456+789", "888"));
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(
            () ->
                new FugaziWP().toPatientTypeCoordinatesOrDie("cannot-parse", Patient.class, "888"));
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> new BadIdWP().toPatientTypeCoordinatesOrDie("x", Patient.class, "888"));
  }

  @Test
  void toProviderTypeCoordinates() {
    assertThat(new FugaziWP().toRecordCoordinatesOrDie("fugazi:ABC;123;456", Patient.class))
        .isEqualTo(RecordCoordinates.fromString("ABC;123;456"));
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> new FugaziWP().toRecordCoordinatesOrDie("cannot-parse", Patient.class));
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> new BadIdWP().toRecordCoordinatesOrDie("x", Patient.class));
  }

  static class BadIdWP implements WitnessProtection {
    @Override
    public <R extends Resource> String privateIdForResourceOrDie(
        String publicId, Class<R> resourceType) {
      return toPrivateId(publicId);
    }

    @Override
    public String toPrivateId(String publicId) {
      throw new BadId("fugazi");
    }

    @Override
    public <R extends Resource> String toPublicId(Class<R> resourceType, String privateId) {
      return toPrivateId(privateId);
    }
  }

  static class FugaziWP implements WitnessProtection {
    @Override
    public <R extends Resource> String privateIdForResourceOrDie(
        String publicId, Class<R> resourceType) {
      return toPrivateId(publicId);
    }

    @Override
    public String toPrivateId(String publicId) {
      return publicId.replace("fugazi:", "");
    }

    @Override
    public <R extends Resource> String toPublicId(Class<R> resourceType, String privateId) {
      return "fugazi:" + privateId;
    }
  }
}
