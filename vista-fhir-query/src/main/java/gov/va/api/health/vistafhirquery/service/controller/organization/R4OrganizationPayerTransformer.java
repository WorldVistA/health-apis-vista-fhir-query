package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.fhir.api.Safe.stream;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.emptyToNull;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class R4OrganizationPayerTransformer {
  /** The fields needed by the transformer. */
  // Add DEACTIVATED below after API-10580 is completed
  public static final List<String> REQUIRED_FIELDS =
      List.of(Payer.EDI_ID_NUMBER_INST, Payer.EDI_ID_NUMBER_PROF, Payer.PAYER_NAME);

  @NonNull String site;

  @NonNull Results rpcResults;

  private Identifier identifier(String code, Optional<String> value) {
    if (isBlank(value)) {
      return null;
    }
    return Identifier.builder()
        .type(
            CodeableConcept.builder().coding(Coding.builder().code(code).build().asList()).build())
        .value(value.get())
        .build();
  }

  private List<Identifier> identifiers(FilemanEntry entry) {
    var identifiers =
        Stream.of(
                identifier("PROFEDI", entry.external(Payer.EDI_ID_NUMBER_PROF)),
                identifier("INSTEDI", entry.external(Payer.EDI_ID_NUMBER_INST)))
            .filter(Objects::nonNull)
            .toList();
    return emptyToNull(identifiers);
  }

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

  /** Transform an RPC response to fhir. */
  public Stream<Organization> toFhir() {
    return stream(rpcResults().results())
        .filter(Objects::nonNull)
        .filter(r -> Payer.FILE_NUMBER.equals(r.file()))
        .map(this::toOrganization)
        .filter(Objects::nonNull);
  }

  private Organization toOrganization(FilemanEntry entry) {
    if (isBlank(entry) || isBlank(entry.fields())) {
      return null;
    }
    return Organization.builder()
        .meta(Meta.builder().source(site()).build())
        .identifier(identifiers(entry))
        .id(
            RecordCoordinates.builder()
                .site(site())
                .file(Payer.FILE_NUMBER)
                .ien(entry.ien())
                .build()
                .toString())
        .type(payer().asList())
        .name(entry.external(Payer.PAYER_NAME).orElse(null))
        .active(entry.external(Payer.DEACTIVATED, "NO"::equals).orElse(null))
        .build();
  }
}
