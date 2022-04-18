package gov.va.api.health.vistafhirquery.service.api;

import gov.va.api.health.r4.api.resources.Appointment;
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

public interface R4AppointmentApi {
  @GET
  @Operation(
      summary = "Appointment Read",
      description = "https://www.hl7.org/fhir/R4/appointment.html",
      tags = {"Appointment"})
  @Path("/hcs/{site}/r4/Appointment/{id}")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Appointment.class))
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
  Appointment appointmentRead(
      @Parameter(hidden = true) HttpServletRequest request,
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
      summary = "Appointment Search",
      description = "https://www.hl7.org/fhir/R4/appointment.html",
      tags = {"Appointment"})
  @Path("/hcs/{site}/r4/Appointment")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content =
            @Content(
                mediaType = "application/fhir+json",
                schema = @Schema(implementation = Appointment.Bundle.class))),
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
  Appointment.Bundle appointmentSearch(
      @Parameter(hidden = true) HttpServletRequest request,
      @Parameter(
              in = ParameterIn.PATH,
              name = "site",
              required = true,
              description = "The id of the site where this resource can be found.")
          String site,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "date",
              description =
                  "A date or range of dates (maximum of 2) that describes the date that "
                      + "the appointment was recorded.")
          @Size(max = 2)
          String[] date,
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
