package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import gov.va.api.health.vistafhirquery.service.util.BiMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/** Interface facilitate interaction with with alternate ids. */
public interface AlternatePatientIds {

  /** Return true if the given parameter is a known patient ID type request parameter. */
  default boolean isPatientIdParameter(String parameter) {
    return patientIdParameters().contains(parameter);
  }

  /** Return the HTTP request parameters that are can have patient IDs. */
  List<String> patientIdParameters();

  /**
   * Return the private ID for the given public ID if available. If no such private ID is available,
   * the given public ID will be returned.
   */
  String toPrivateId(String publicId);

  /**
   * Return the public ID for the given private ID if available. If no such public ID is available,
   * the given private ID will be returned.
   */
  String toPublicId(String privateId);

  /** DisabledAlternatePatientIds. */
  class DisabledAlternatePatientIds implements AlternatePatientIds {

    @Override
    public List<String> patientIdParameters() {
      return List.of();
    }

    @Override
    public String toPrivateId(String publicId) {
      return publicId;
    }

    @Override
    public String toPublicId(String privateId) {
      return privateId;
    }
  }

  /** MappedAlternatePatientIds. */
  @Value
  class MappedAlternatePatientIds implements AlternatePatientIds {

    private BiMap<String, String> publicToPrivateIds;

    @Getter private List<String> patientIdParameters;

    @Builder
    public MappedAlternatePatientIds(
        List<String> patientIdParameters, Map<String, String> publicToPrivateIds) {
      this.patientIdParameters = List.copyOf(patientIdParameters);
      this.publicToPrivateIds = new BiMap<>(publicToPrivateIds);
    }

    @Override
    public String toPrivateId(String publicId) {

      return publicToPrivateIds.leftToRight(publicId, publicId);
    }

    @Override
    public String toPublicId(String privateId) {
      return publicToPrivateIds.rightToLeft(privateId, privateId);
    }
  }
}
