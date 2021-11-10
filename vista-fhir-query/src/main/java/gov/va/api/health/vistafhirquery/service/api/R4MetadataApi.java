package gov.va.api.health.vistafhirquery.service.api;

import gov.va.api.health.r4.api.resources.CapabilityStatement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface R4MetadataApi {
  @Operation(
      security = {},
      summary = "Capability Statement",
      description = "http://hl7.org/fhir/R4/capabilitystatement.html",
      tags = "Metadata")
  @GET
  @Path("metadata")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = CapabilityStatement.class)))
  CapabilityStatement metadata();
}
