package gov.va.api.health.vistafhirquery.service.api;

import gov.va.api.health.r4.api.resources.Condition;
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

/** ConditionApi swagger documentation for VFQ. */
public interface R4ConditionApi {
  @Operation(
      summary = "Condition Read",
      description = "http://hl7.org/fhir/us/core/StructureDefinition-us-core-condition.html",
      tags = {"Condition"})
  @GET
  @Path("/hcs/{site}/r4/Condition/{id}")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content = {
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Condition.class))
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
  Condition conditionRead(
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
              example = "I3-Sy4Pfqamhh8y62pa15zJwDkHVa1Jr3nM")
          String id);

  @Operation(
      summary = "Condition Search",
      description = "http://www.hl7.org/fhir/us/core/StructureDefinition-us-core-condition.html",
      tags = {"Condition"})
  @GET
  @Path("/hcs/{site}/r4/Condition")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Record found",
        content = {
          @Content(
              schema = @Schema(implementation = Condition.Bundle.class),
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
  Condition.Bundle conditionSearch(
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
                  "The general classification of the type of condition. "
                      + "[Condition Category Codes](http://www.hl7.org/fhir/us/core/ValueSet-us-core-condition-category.html)")
          String category,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "clinical-status",
              description =
                  "The subject's current status in relation to the condition. "
                      + "[Condition Clinical Status Codes](http://hl7.org/fhir/R4/valueset-condition-clinical.html)")
          String clinicalStatus,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "code",
              description =
                  "The SNOMED CT, ICD-9, or ICD-10 code of the condition. "
                      + "[Condition Codes](http://www.hl7.org/fhir/us/core/ValueSet-us-core-condition-code.html)")
          String code,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "onset-date",
              description =
                  "A date or range of dates (maximum of 2) that describes the date that "
                      + "the patient claims to have the condition.")
          @Size(max = 2)
          String[] date,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description =
                  "The number of resources that should be returned in a single page. "
                      + "The maximum count size is 100.")
          @DefaultValue("30")
          int count);
}
