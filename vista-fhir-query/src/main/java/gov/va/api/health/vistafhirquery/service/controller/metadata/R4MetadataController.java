package gov.va.api.health.vistafhirquery.service.controller.metadata;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactDetail;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import gov.va.api.health.r4.api.resources.CapabilityStatement.CapabilityResource;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Implementation;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Kind;
import gov.va.api.health.r4.api.resources.CapabilityStatement.ReferencePolicy;
import gov.va.api.health.r4.api.resources.CapabilityStatement.ResourceInteraction;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Rest;
import gov.va.api.health.r4.api.resources.CapabilityStatement.RestMode;
import gov.va.api.health.r4.api.resources.CapabilityStatement.SearchParamType;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Security;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Software;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Status;
import gov.va.api.health.r4.api.resources.CapabilityStatement.TypeRestfulInteraction;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Versioning;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = {"/r4/metadata"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
class R4MetadataController {
  private final LinkProperties pageLinks;

  private final MetadataProperties properties;

  private final BuildProperties buildProperties;

  private List<ContactDetail> contact() {
    return List.of(
        ContactDetail.builder()
            .name(properties.getContact().getName())
            .telecom(
                List.of(
                    ContactPoint.builder()
                        .system(ContactPointSystem.email)
                        .value(properties.getContact().getEmail())
                        .build()))
            .build());
  }

  private Implementation implementation() {
    return Implementation.builder()
        .description(properties.getR4().getName())
        .url(pageLinks.getPublicUrl() + "/r4")
        .build();
  }

  @GetMapping
  public CapabilityStatement read() {
    return CapabilityStatement.builder()
        .resourceType("CapabilityStatement")
        .id(properties.getR4().getId())
        .version(properties.getVersion())
        .name(properties.getR4().getName())
        .title(properties.getR4().getName())
        .publisher(properties.getPublisher())
        .status(Status.active)
        .implementation(implementation())
        .experimental(!properties.isProductionUse())
        .contact(contact())
        .date(properties.getPublicationDate())
        .description(properties.getDescription())
        .kind(Kind.capability)
        .software(software())
        .fhirVersion("4.0.1")
        .format(asList("application/json", "application/fhir+json"))
        .rest(rest())
        .build();
  }

  private List<CapabilityResource> resources() {
    return supportedResources().map(SupportedResource::asResource).collect(toList());
  }

  private List<Rest> rest() {
    return List.of(
        Rest.builder()
            .mode(RestMode.server)
            .security(restSecurity())
            .resource(resources())
            .build());
  }

  private Security restSecurity() {
    return Security.builder()
        .cors(true)
        .description(properties.getSecurity().getDescription())
        .service(List.of(smartOnFhirCodeableConcept()))
        .extension(
            List.of(
                Extension.builder()
                    .url("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris")
                    .extension(
                        List.of(
                            Extension.builder()
                                .url("token")
                                .valueUri(properties.getSecurity().getTokenEndpoint())
                                .build(),
                            Extension.builder()
                                .url("authorize")
                                .valueUri(properties.getSecurity().getAuthorizeEndpoint())
                                .build(),
                            Extension.builder()
                                .url("manage")
                                .valueUri(properties.getSecurity().getManagementEndpoint())
                                .build(),
                            Extension.builder()
                                .url("revoke")
                                .valueUri(properties.getSecurity().getRevocationEndpoint())
                                .build()))
                    .build()))
        .build();
  }

  private CodeableConcept smartOnFhirCodeableConcept() {
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/restful-security-service")
                    .code("SMART-on-FHIR")
                    .display("SMART-on-FHIR")
                    .build()))
        .build();
  }

  private Software software() {
    return Software.builder()
        .name(buildProperties.getGroup() + ":" + buildProperties.getArtifact())
        .releaseDate(buildProperties.getTime().toString())
        .version(buildProperties.getVersion())
        .build();
  }

  private SupportedResource.SupportedResourceBuilder support(String type) {
    return SupportedResource.builder().properties(properties).type(type);
  }

  private Stream<SupportedResource> supportedResources() {
    return Stream.of(
        support("Appointment")
            .profileUrl("https://www.hl7.org/fhir/r4/appointment.html")
            .search(Set.of(SearchParam.DATE, SearchParam.PATIENT))
            .build(),
        support("Condition")
            .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition")
            .search(
                Set.of(
                    SearchParam.CATEGORY,
                    SearchParam.CLINICAL_STATUS,
                    SearchParam.CODE,
                    SearchParam.ID,
                    SearchParam.IDENTIFIER,
                    SearchParam.ONSET_DATE,
                    SearchParam.PATIENT))
            .build(),
        support("Coverage")
            .profileUrl("http://hl7.org/fhir/us/carin/StructureDefinition/carin-bb-coverage")
            .search(Set.of())
            .build(),
        support("Endpoint")
            .profileUrl("https://www.hl7.org/fhir/r4/endpoint.html")
            .search(Set.of(SearchParam.PATIENT, SearchParam.STATUS, SearchParam.TAG))
            .build(),
        support("InsurancePlan")
            .profileUrl("https://www.hl7.org/fhir/r4/insuranceplan.html")
            .search(Set.of(SearchParam.IDENTIFIER))
            .build(),
        support("Observation")
            .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab")
            .search(
                Set.of(
                    SearchParam.ID,
                    SearchParam.IDENTIFIER,
                    SearchParam.PATIENT,
                    SearchParam.CATEGORY,
                    SearchParam.CODE,
                    SearchParam.DATE))
            .build(),
        support("Organization")
            .profileUrl("http://hl7.org/fhir/us/carin/StructureDefinition/carin-bb-organization")
            .search(Set.of(SearchParam.TYPE))
            .build());
  }

  @Getter
  @AllArgsConstructor
  enum SearchParam {
    CATEGORY("category", SearchParamType.token),
    CLINICAL_STATUS("clinical-status", SearchParamType.token),
    CODE("code", SearchParamType.token),
    DATE("date", SearchParamType.date),
    ID("_id", SearchParamType.token),
    IDENTIFIER("identifier", SearchParamType.token),
    ONSET_DATE("onset-date", SearchParamType.date),
    PATIENT("patient", SearchParamType.reference),
    STATUS("status", SearchParamType.token),
    TAG("tag", SearchParamType.string),
    TYPE("type", SearchParamType.token);

    private final String param;

    private final SearchParamType type;
  }

  @Value
  @Builder
  private static class SupportedResource {
    String type;

    String profileUrl;

    @Builder.Default @NonNull Set<SearchParam> search = emptySet();

    MetadataProperties properties;

    CapabilityResource asResource() {
      return CapabilityResource.builder()
          .type(type)
          .profile(profileUrl)
          .interaction(interactions())
          .searchParam(searchParams())
          .versioning(Versioning.no_version)
          .referencePolicy(List.of(ReferencePolicy.literal, ReferencePolicy.local))
          .build();
    }

    private List<ResourceInteraction> interactions() {
      if (search.isEmpty()) {
        return List.of(readable());
      }
      return List.of(searchable(), readable());
    }

    private ResourceInteraction readable() {
      return ResourceInteraction.builder()
          .code(TypeRestfulInteraction.read)
          .documentation(properties.getR4().getResourceDocumentation())
          .build();
    }

    private List<CapabilityStatement.SearchParam> searchParams() {
      if (search.isEmpty()) {
        return null;
      }
      return search.stream()
          .sorted((a, b) -> a.param().compareTo(b.param()))
          .map(
              s -> CapabilityStatement.SearchParam.builder().name(s.param()).type(s.type()).build())
          .collect(toList());
    }

    private ResourceInteraction searchable() {
      return ResourceInteraction.builder()
          .code(TypeRestfulInteraction.search_type)
          .documentation(properties.getR4().getResourceDocumentation())
          .build();
    }
  }
}
