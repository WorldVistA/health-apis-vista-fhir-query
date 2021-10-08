package gov.va.api.health.vistafhirquery.service.controller.organization;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Values;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;

public class OrganizationPayerSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    private Identifier identifier(String code, String value) {
      return Identifier.builder()
          .type(
              CodeableConcept.builder()
                  .coding(Coding.builder().code(code).build().asList())
                  .build())
          .value(value)
          .build();
    }

    public Organization organization(String site, String ien) {
      return Organization.builder()
          .meta(Meta.builder().source(site).build())
          .id(
              RecordCoordinates.builder()
                  .site(site)
                  .file(Payer.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .identifier(List.of(identifier("PROFEDI", "EXT:PROF"), identifier("INSTEDI", "EXT:INST")))
          .type(
              CodeableConcept.builder()
                  .coding(
                      Coding.builder()
                          .system("http://terminology.hl7.org/CodeSystem/organization-type")
                          .code("pay")
                          .display("Payer")
                          .build()
                          .asList())
                  .build()
                  .asList())
          .name("ROSE APOTHECARY")
          .active(true)
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    private Map<String, Values> fields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(Payer.PAYER_NAME, Values.of("ROSE APOTHECARY", "APOTHECARY"));
      fields.put(Payer.DEACTIVATED, Values.of("NO", "0"));
      fields.put(Payer.EDI_ID_NUMBER_INST, Values.of("EXT:INST", "IN:INST"));
      fields.put(Payer.EDI_ID_NUMBER_PROF, Values.of("EXT:PROF", "IN:PROF"));
      return fields;
    }

    public Results getsManifestResults() {
      return getsManifestResults("8");
    }

    public Results getsManifestResults(String id) {
      return Results.builder()
          .results(
              List.of(
                  FilemanEntry.builder().file(Payer.FILE_NUMBER).ien(id).fields(fields()).build()))
          .build();
    }
  }
}
