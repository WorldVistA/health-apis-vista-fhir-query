package gov.va.api.health.vistafhirquery.service.api;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Organization;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

public interface R4OrganizationApi {
  @POST
  @Operation(
      summary = "Organization Create",
      description =
          "https://hl7.org/fhir/us/core/STU4/StructureDefinition-us-core-organization.html",
      tags = {"Organization"})
  @Path("/hcs/{site}/Organization")
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
  void organizationCreate(
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
                    schema = @Schema(implementation = Organization.class)),
                @Content(
                    mediaType = "application/fhir+json",
                    schema = @Schema(implementation = Organization.class))
              },
              description = "The resource to be created.")
          Organization body);

  @Operation(
      summary = "Organization Read",
      description =
          "https://hl7.org/fhir/us/core/STU4/StructureDefinition-us-core-organization.html",
      tags = {"Organization"})
  @GET
  @Path("/hcs/{site}/Organization/{id}")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Organization.class))),
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
        responseCode = "500",
        description = "Server Error",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        })
  })
  Organization organizationRead(
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
                  "The logical id of the resource. Once assigned, this value never changes.",
              example = "I3-iYUBEbvodmvCg3XYdjLHtoVJkL57MMRWNXkvtfAcg2Q")
          String id);

  @Operation(
      summary = "Organization Search",
      description =
          "https://hl7.org/fhir/us/core/STU4/StructureDefinition-us-core-organization.html",
      tags = {"Organization"})
  @GET
  @Path("/hcs/{site}/Organization")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record(s) found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Organization.Bundle.class))),
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
        responseCode = "500",
        description = "Server Error",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class))
        })
  })
  Organization.Bundle organizationSearch(
      @Parameter(hidden = true) HttpServletRequest httpRequest,
      @Parameter(
              in = ParameterIn.PATH,
              name = "site",
              required = true,
              description = "The id of the site where this resource can be found.")
          String site,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "type",
              required = true,
              description = "The type of organization to return results for.")
          String type,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description = "The number of resources that should be returned in a single page.")
          int count);

  @PUT
  @Operation(
      summary = "Organization Update",
      description =
          "https://hl7.org/fhir/us/core/STU4/StructureDefinition-us-core-organization.html",
      tags = {"Organization"})
  @Path("/hcs/{site}/Organization/{id}")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Record updated."),
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
  void organizationUpdate(
      @Parameter(hidden = true) HttpServletResponse response,
      @Parameter(
              in = ParameterIn.PATH,
              name = "site",
              required = true,
              description = "The id of the site where this resource should be created.")
          String site,
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description = "The id of the resource that will be updated.")
          String id,
      @RequestBody(
              required = true,
              content = {
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Organization.class)),
                @Content(
                    mediaType = "application/fhir+json",
                    schema = @Schema(implementation = Organization.class))
              },
              description = "The resource to be created.")
          Organization body);
}
