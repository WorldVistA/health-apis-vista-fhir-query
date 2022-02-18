package gov.va.api.health.vistafhirquery.service.controller;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class IdentifierRecord {
  String system;

  String fieldNumber;

  boolean isRequired;
}
