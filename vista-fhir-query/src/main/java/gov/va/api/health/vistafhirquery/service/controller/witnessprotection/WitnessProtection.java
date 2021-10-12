package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import java.util.function.Function;

/** Interface for translating publicId to a privateId. */
public interface WitnessProtection {
  /**
   * Attempt to decode a private ID using a given transformer. BadId and IllegalArgumentExceptions
   * will result in a NotFound exception.
   */
  default <T> T decodePrivateId(String publicId, Function<String, T> decoder) {
    try {
      return decoder.apply(toPrivateId(publicId));
    } catch (IdEncoder.BadId | IllegalArgumentException e) {
      throw ResourceExceptions.NotFound.because("Unsupported id %s", publicId);
    }
  }

  <R extends Resource> String privateIdForResourceOrDie(String publicId, Class<R> resourceType);

  /** Try to parse patient type coordinates given a public id. */
  default <R extends Resource> PatientTypeCoordinates toPatientTypeCoordinatesOrDie(
      String publicId, Class<R> resourceType) {
    try {
      return PatientTypeCoordinates.fromString(privateIdForResourceOrDie(publicId, resourceType));
    } catch (IdEncoder.BadId | IllegalArgumentException e) {
      throw ResourceExceptions.NotFound.because("Unsupported id %s", publicId);
    }
  }

  String toPrivateId(String publicId);

  <R extends Resource> String toPublicId(Class<R> resourceType, String privateId);

  /** Try to parse record coordinates given a public id. */
  default <R extends Resource> RecordCoordinates toRecordCoordinatesOrDie(
      String publicId, Class<R> resourceType) {
    try {
      return RecordCoordinates.fromString(privateIdForResourceOrDie(publicId, resourceType));
    } catch (IdEncoder.BadId | IllegalArgumentException e) {
      throw ResourceExceptions.NotFound.because("Unsupported id %s", publicId);
    }
  }
}
