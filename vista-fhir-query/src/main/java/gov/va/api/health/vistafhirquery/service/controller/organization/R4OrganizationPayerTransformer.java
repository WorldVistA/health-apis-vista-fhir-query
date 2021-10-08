package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class R4OrganizationPayerTransformer {
  /** The fields needed by the transformer. */
  // Add DEACTIVATED below after API-10580 is completed
  public static final List<String> REQUIRED_FIELDS = List.of(Payer.PAYER_NAME);

  @NonNull Map.Entry<String, LhsLighthouseRpcGatewayResponse.Results> rpcResults;

  private CodeableConcept payer() {
    return CodeableConcept.builder()
        .coding(
            Coding.builder()
                .code("pay")
                .display("Payer")
                .system("http://terminology.hl7.org/CodeSystem/organization-type")
                .build()
                .asList())
        .build();
  }

  private String site() {
    return rpcResults().getKey();
  }

  /** Transform an RPC response to fhir. */
  public Stream<Organization> toFhir() {
    return rpcResults().getValue().results().stream()
        .filter(Objects::nonNull)
        .filter(r -> Payer.FILE_NUMBER.equals(r.file()))
        .map(this::toOrganization)
        .filter(Objects::nonNull);
  }

  private Organization toOrganization(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    if (entry == null || isBlank(entry.fields())) {
      return null;
    }
    return Organization.builder()
        .meta(Meta.builder().source(site()).build())
        .id(
            RecordCoordinates.builder()
                .site(site())
                .file(Payer.FILE_NUMBER)
                .ien(entry.ien())
                .build()
                .toString())
        .type(payer().asList())
        .name(entry.external(Payer.PAYER_NAME).orElse(null))
        .active(entry.external(Payer.DEACTIVATED, "NO"::equals).orElse(false))
        .build();
  }
}
