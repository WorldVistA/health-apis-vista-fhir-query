package gov.va.api.health.vistafhirquery.service.api;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

public interface R4CoverageApi {
  @POST
  @Operation(
      summary = "Coverage Create",
      description = "http://hl7.org/fhir/us/carin/StructureDefinition/carin-bb-coverage",
      tags = {"Coverage"})
  @Path("/hcs/{site}/r4/Coverage")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Record created"),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = OperationOutcome.class))),
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
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = OperationOutcome.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Unprocessable Entity",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        })
  })
  void coverageCreate(
      @Parameter(hidden = true) HttpServletResponse response,
      @Parameter(
              in = ParameterIn.PATH,
              name = "site",
              required = true,
              description = "The id of the site where this resource should be created.")
          String site,
      @RequestBody(
              required = true,
              content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Coverage.class)),
                @Content(
                    mediaType = "application/fhir+json",
                    schema = @Schema(implementation = Coverage.class))
              },
              description = "The resource to be created.")
          Coverage body);

  @GET
  @Operation(
      summary = "Coverage Read",
      description = "http://hl7.org/fhir/us/carin/StructureDefinition/carin-bb-coverage",
      tags = {"Coverage"})
  @Path("/hcs/{site}/r4/Coverage/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Coverage.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Coverage coverageRead(
      @Parameter(
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
      summary = "Coverage Search",
      description = "http://hl7.org/fhir/us/carin/StructureDefinition/carin-bb-coverage",
      tags = {"Coverage"})
  @Path("/hcs/{site}/r4/Coverage")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Coverage.Bundle.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Coverage.Bundle coverageSearch(
      @Parameter(hidden = true) HttpServletRequest request,
      @Parameter(
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
                      + " assigned by the Master Patient Index (MPI).")
          String icn,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "page",
              description = "The page number to be returned.")
          Integer page,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description =
                  "The number of resources that should be returned in a single page. "
                      + "The maximum count size is 100.")
          @DefaultValue("30")
          int count);
}
