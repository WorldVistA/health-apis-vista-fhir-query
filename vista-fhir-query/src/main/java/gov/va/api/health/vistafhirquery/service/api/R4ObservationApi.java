package gov.va.api.health.vistafhirquery.service.api;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/** ObservationApi swagger documentation for VFQ. */
public interface R4ObservationApi {
  @Operation(
      summary = "Observation Read",
      description = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab",
      tags = {"Observation"})
  @GET
  @Path("/Observation/{id}")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Observation.class))
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
  Observation observationRead(
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
                  "The logical id of the resource. Once assigned, this value never changes.",
              example = "I3-MzfzyZkSpl9HvWWWuN0JvxF6V2f0fwrUm4Cj381IfxH")
          String id);

  @Operation(
      summary = "Observation Search",
      description = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab",
      tags = {"Observation"})
  @GET
  @Path("/Observation")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content = {
          @Content(
              schema = @Schema(implementation = Observation.Bundle.class),
              mediaType = "application/fhir+json")
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
  Observation.Bundle observationSearch(
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
              description =
                  "The patient's Integration Control Number (ICN)"
                      + " assigned by the Master Patient Index (MPI).",
              example = "1011537977V693883")
          String patient,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "identifier",
              description =
                  "The logical identifier of the resource. "
                      + "Once assigned, this value never changes.")
          String identifier,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "category",
              description =
                  "The general classification of the type of observation. "
                      + "[Observation Category Codes](https://www.hl7.org/fhir/valueset-observation-category.html)")
          String category,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "code",
              description =
                  "A code that indicates the type of information contained within the observation. "
                      + "[LOINC Observation Codes](https://www.hl7.org/fhir/valueset-observation-codes.html)")
          String code,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "date",
              description =
                  "A date or range of dates (maximum of 2) that describes the date that "
                      + "the observation was recorded.")
          @Size(max = 2)
          String[] date,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description = "The number of resources that should be returned in a single page.")
          @DefaultValue("30")
          int count);
}
