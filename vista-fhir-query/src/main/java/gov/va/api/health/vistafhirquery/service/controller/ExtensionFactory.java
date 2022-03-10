package gov.va.api.health.vistafhirquery.service.controller;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableCodeableConceptDefinition;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableExtensionDefinition;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class ExtensionFactory {
  LhsLighthouseRpcGatewayResponse.FilemanEntry entry;

  Map<String, Boolean> yesNo;

  /** Builds an extension with a valueCodeableConcept. */
  private Extension buildExtensionWithCodeableConcept(String value, String system, String url) {
    return Extension.builder()
        .valueCodeableConcept(
            CodeableConcept.builder()
                .coding(Coding.builder().code(value).system(system).build().asList())
                .build())
        .url(url)
        .build();
  }

  /** Creates a valueCode extension. */
  public Extension ofCode(String fieldNumber, String url) {
    var value = entry.internal(fieldNumber);
    if (value.isEmpty()) {
      return null;
    }
    return Extension.builder().url(url).valueCode(value.get()).build();
  }

  /** Creates a valueCodeableConcept extension with an external value. */
  public Extension ofCodeableConceptFromExternalValue(
      MappableExtensionDefinition<MappableCodeableConceptDefinition> definition) {
    return ofCodeableConceptFromExternalValue(
        definition.valueDefinition().vistaField(),
        definition.valueDefinition().valueSet(),
        definition.structureDefinition());
  }

  /** Creates a valueCodeableConcept extension with an external value. */
  public Extension ofCodeableConceptFromExternalValue(
      String fieldNumber, String system, String url) {
    if (isBlank(fieldNumber)) {
      return null;
    }
    return entry
        .external(fieldNumber)
        .map(value -> buildExtensionWithCodeableConcept(value, system, url))
        .orElse(null);
  }

  /** Creates a valueCodeableConcept extension with an internal value. */
  public Extension ofCodeableConceptFromInternalValue(
      String fieldNumber, String system, String url) {
    if (isBlank(fieldNumber)) {
      return null;
    }
    return entry
        .internal(fieldNumber)
        .map(value -> buildExtensionWithCodeableConcept(value, system, url))
        .orElse(null);
  }

  /** Creates a valueQuantity extension. */
  public Extension ofQuantity(String fieldNumber, String unit, String system, String url) {
    var value = entry.internal(fieldNumber);
    if (value.isEmpty()) {
      return null;
    }
    BigDecimal quantity;
    try {
      quantity = new BigDecimal(value.get());
    } catch (NumberFormatException e) {
      throw new LhsLighthouseRpcGatewayResponse.UnexpectedVistaValue(fieldNumber, value, e);
    }
    return Extension.builder()
        .url(url)
        .valueQuantity(Quantity.builder().value(quantity).unit(unit).system(system).build())
        .build();
  }

  /** Creates a valueReference extension. */
  public Extension ofReference(String resource, String id, String url) {
    if (isAnyBlank(resource, id, url)) {
      return null;
    }
    return Extension.builder()
        .url(url)
        .valueReference(Reference.builder().reference(resource + "/" + id).build())
        .build();
  }

  /** Creates a valueString extension. */
  public Extension ofString(String fieldNumber, String url) {
    var value = entry.internal(fieldNumber);
    if (value.isEmpty()) {
      return null;
    }
    return Extension.builder().url(url).valueString(value.get()).build();
  }

  /** Creates a valueBoolean extension. */
  public Extension ofYesNoBoolean(String fieldNumber, String url) {
    var value = entry.internal(fieldNumber, yesNo);
    if (value.isEmpty()) {
      return null;
    }
    return Extension.builder().valueBoolean(value.get()).url(url).build();
  }
}
