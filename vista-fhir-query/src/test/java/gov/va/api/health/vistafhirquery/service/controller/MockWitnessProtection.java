package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class MockWitnessProtection implements WitnessProtection {

  @Getter private final Map<String, String> publicToPrivateId = new HashMap<>();

  public MockWitnessProtection add(String publicId, String privateId) {
    publicToPrivateId().put(publicId, privateId);
    return this;
  }

  @Override
  public <R extends Resource> String privateIdForResourceOrDie(
      String publicId, Class<R> resourceType) {
    return toPrivateId(publicId);
  }

  @Override
  public String toPrivateId(String publicId) {
    return publicToPrivateId().get(publicId);
  }
}
