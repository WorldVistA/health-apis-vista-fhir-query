package gov.va.api.health.vistafhirquery.service.api;

import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
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

public interface R4CoverageEligibilityResponseApi {
  @POST
  @Operation(
      summary = "CoverageEligibilityResponse Create",
      description = "http://hl7.org/fhir/R4/coverageeligibilityresponse.html",
      tags = {"CoverageEligibilityResponse"})
  @Path("/hcs/{site}/CoverageEligibilityResponse")
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
  void coverageEligibilityResponseCreate(
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
                    mediaType = "application/fhir+json",
                    schema = @Schema(implementation = CoverageEligibilityResponse.class))
              },
              description = "The resource to be created.")
          CoverageEligibilityResponse body);

  @GET
  @Operation(
      summary = "CoverageEligibilityResponse Search",
      description = "http://hl7.org/fhir/R4/coverageeligibilityresponse.html",
      tags = {"CoverageEligibilityResponse"})
  @Path("/hcs/{site}/CoverageEligibilityResponse")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = CoverageEligibilityResponse.Bundle.class)))
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
  CoverageEligibilityResponse.Bundle coverageEligibilityResponseSearch(
      @Parameter(hidden = true) HttpServletRequest httpRequest,
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
              name = "_count",
              description = "The number of resources that should be returned in a single page.")
          @DefaultValue("30")
          int count);
}
