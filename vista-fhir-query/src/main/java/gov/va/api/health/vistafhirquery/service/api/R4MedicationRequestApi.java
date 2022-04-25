package gov.va.api.health.vistafhirquery.service.api;

import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface R4MedicationRequestApi {

  @GET
  @Operation(
      summary = "Medication Request Read",
      description = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest",
      tags = {"MedicationRequest"})
  @Path("/MedicationRequest/{id}")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = MedicationRequest.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "500",
        description = "Server Error",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        })
  })
  MedicationRequest medicationRequestRead(
      @Parameter(hidden = true) HttpServletRequest request,
      @Parameter(
              hidden = true,
              in = ParameterIn.PATH,
              name = "site",
              required = true,
              description = "The id of the site where this resource can be found.")
          String site,
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @GET
  @Operation(
      summary = "Medication Request Search",
      description = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest",
      tags = {"MedicationRequest"})
  @Path("/MedicationRequest")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = MedicationRequest.Bundle.class))
        }),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        }),
    @ApiResponse(
        responseCode = "500",
        description = "Server Error",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        })
  })
  MedicationRequest.Bundle medicationRequestSearch(
      @Parameter(hidden = true) HttpServletRequest request,
      @Parameter(
              hidden = true,
              in = ParameterIn.PATH,
              name = "site",
              required = true,
              description = "The id of the site where this resource can be found.")
          String site,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "patient",
              required = true,
              description =
                  "The patient's Integration Control Number (ICN)"
                      + " assigned by the Master Patient Index (MPI).")
          String icn,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description = "The number of resources that should be returned in a single page.")
          @DefaultValue("30")
          int count);
}
