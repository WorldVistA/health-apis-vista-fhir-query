package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.IdentitySubstitution;
import gov.va.api.health.ids.api.IsTransformable;
import gov.va.api.health.ids.api.PrivateToPublicIdTransformation;
import gov.va.api.health.ids.api.PublicToPrivateIdTransformation;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.ids.client.IdEncoder.BadId;
import gov.va.api.health.r4.api.Fhir;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.vistafhirquery.service.controller.FhirCompliantCharacters;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.EmptyRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Controller advice is automatically applied to Bundle and Resource responses. It will lookup the
 * appropriate witness protection agent based on the resource type and substitute references to
 * private IDs to public Ids.
 */
@Slf4j
@ControllerAdvice
public class WitnessProtectionAdvice extends IdentitySubstitution<ProtectedReference>
    implements RequestBodyAdvice, ResponseBodyAdvice<Object>, WitnessProtection {
  private final ProtectedReferenceFactory protectedReferenceFactory;

  private final AlternatePatientIds alternatePatientIds;

  private final Map<Type, WitnessProtectionAgent<?>> agents;

  private boolean clearIds;

  /** Create a new instance. */
  @Builder
  @Autowired
  public WitnessProtectionAdvice(
      @NonNull ProtectedReferenceFactory protectedReferenceFactory,
      @NonNull AlternatePatientIds alternatePatientIds,
      @NonNull IdentityService identityService,
      List<WitnessProtectionAgent<?>> availableAgents,
      @org.springframework.beans.factory.annotation.Value("${vista-fhir-query.clear-ids:false}")
          boolean clearIds) {
    super(
        clearIds ? new ClearIdentityService(identityService) : identityService,
        ProtectedReference::asResourceIdentity,
        NotFound::new);
    this.protectedReferenceFactory = protectedReferenceFactory;
    this.alternatePatientIds = alternatePatientIds;
    this.clearIds = clearIds;
    this.agents =
        availableAgents.stream().collect(toMap(WitnessProtectionAdvice::agentType, identity()));
    log.info("Clear ID support enabled: {}", clearIds);
    log.info(
        "Witness protection is available for {}",
        agents.keySet().stream().map(t -> ((Class<?>) t).getSimpleName()).collect(joining(", ")));
  }

  private static Type agentType(WitnessProtectionAgent<?> agent) {
    Type agentInterface =
        Stream.of(agent.getClass().getGenericInterfaces())
            .filter(
                type -> type.getTypeName().startsWith(WitnessProtectionAgent.class.getName() + "<"))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        agent.getClass() + " is not a " + WitnessProtectionAgent.class.getName()));
    return ((ParameterizedType) agentInterface).getActualTypeArguments()[0];
  }

  @Override
  public Object afterBodyRead(
      @org.springframework.lang.NonNull Object payload,
      @org.springframework.lang.NonNull HttpInputMessage httpInputMessage,
      @org.springframework.lang.NonNull MethodParameter methodParameter,
      @org.springframework.lang.NonNull Type type,
      @org.springframework.lang.NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    var identityOperations =
        IdentityProcessor.builder()
            .transformation(
                PublicToPrivateIdTransformation.<ProtectedReference>builder()
                    .publicIdOf(ProtectedReference::id)
                    .updatePublicIdToPrivateId(ProtectedReference::updateId)
                    .build())
            .replace(
                (resource, operations) -> {
                  var referencesOf =
                      operations.toReferences().andThen(this::alternatePatientPrivateIds);
                  IdentityMapping identities = lookup(List.of(resource), referencesOf);
                  identities.replacePublicIdsWithPrivateIds(List.of(resource), operations);
                })
            .build();
    return process(payload, identityOperations);
  }

  private Stream<ProtectedReference> alternatePatientPrivateIds(
      Stream<ProtectedReference> references) {
    return references.map(r -> processAlternatePatientIds(r, alternatePatientIds::toPrivateId));
  }

  private Stream<ProtectedReference> alternatePatientPublicIds(
      Stream<ProtectedReference> references) {
    return references.map(r -> processAlternatePatientIds(r, alternatePatientIds::toPublicId));
  }

  @Override
  public HttpInputMessage beforeBodyRead(
      @org.springframework.lang.NonNull HttpInputMessage httpInputMessage,
      @org.springframework.lang.NonNull MethodParameter methodParameter,
      @org.springframework.lang.NonNull Type type,
      @org.springframework.lang.NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    // Do Nothing
    return httpInputMessage;
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    var identityOperations =
        IdentityProcessor.builder()
            .transformation(
                PrivateToPublicIdTransformation.<ProtectedReference>builder()
                    .privateIdOf(ProtectedReference::id)
                    .updatePrivateIdToPublicId(ProtectedReference::updateId)
                    .build())
            .replace(
                (resource, operations) -> {
                  var referenceOf =
                      operations.toReferences().andThen(this::alternatePatientPublicIds);
                  IdentityMapping identities = register(List.of(resource), referenceOf);
                  identities.replacePrivateIdsWithPublicIds(List.of(resource), operations);
                })
            .build();
    return process(body, identityOperations);
  }

  @Override
  public Object handleEmptyBody(
      Object o,
      @org.springframework.lang.NonNull HttpInputMessage httpInputMessage,
      @org.springframework.lang.NonNull MethodParameter methodParameter,
      @org.springframework.lang.NonNull Type type,
      @org.springframework.lang.NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    throw new EmptyRequestPayload();
  }

  @Override
  @SneakyThrows
  public <R extends Resource> String privateIdForResourceOrDie(
      @NonNull String publicId, Class<R> resourceType) {
    var resourceIdentity =
        safeLookup(publicId).stream()
            .findFirst()
            .orElseThrow(() -> NotFound.because("Failed to lookup id: %s", publicId));
    if (!"CLEAR".equals(resourceIdentity.system())) {
      var expectedResourceType = protectedReferenceFactory.resourceTypeFor(resourceType);
      if (!expectedResourceType.equals(resourceIdentity.resource())) {
        log.error("Bad ID: Wanted {} {}, got {}", publicId, expectedResourceType, resourceIdentity);
        throw ResourceExceptions.ExpectationFailed.because(
            "Expected id %s to be of type: %s", publicId, expectedResourceType);
      }
    }
    return resourceIdentity.identifier();
  }

  <T> T process(T body, @NonNull IdentityProcessor identityProcessor) {
    if (body instanceof AbstractBundle<?>) {
      processBundle((AbstractBundle<?>) body, identityProcessor);
    } else if (body instanceof Resource) {
      processResource((Resource) body, identityProcessor);
    }
    return body;
  }

  private ProtectedReference processAlternatePatientIds(
      ProtectedReference reference, Function<String, String> alternateIdSubstitution) {
    if (!"Patient".equals(reference.type())) {
      return reference;
    }
    var id = alternateIdSubstitution.apply(reference.id());
    ProtectedReference copyOfReferenceWithAlternateId =
        ProtectedReference.builder()
            .type(reference.type())
            .onUpdate(reference.onUpdate())
            .id(id)
            .build();
    copyOfReferenceWithAlternateId.onUpdate().accept(id);
    return copyOfReferenceWithAlternateId;
  }

  private void processBundle(
      AbstractBundle<?> bundle, @NonNull IdentityProcessor identityProcessor) {
    bundle.entry().forEach(e -> processEntry(e, identityProcessor));
  }

  private void processEntry(AbstractEntry<?> entry, @NonNull IdentityProcessor identityProcessor) {
    Optional<ProtectedReference> referenceToFullUrl =
        protectedReferenceFactory.forUri(
            entry.fullUrl(), entry::fullUrl, protectedReferenceFactory.replaceIdOnly());
    processResource(
        entry.resource(),
        referenceToFullUrl.isEmpty() ? List.of() : List.of(referenceToFullUrl.get()),
        identityProcessor);
  }

  private void processResource(
      @NonNull Resource resource,
      List<ProtectedReference> additionalReferences,
      @NonNull IdentityProcessor identityProcessor) {
    /*
     * We need to work around the compiler safeguards a little here. Since we are in control of the
     * map, we populate the key to be type of the agent. From the map is guaranteed to be type
     * matched. We will have capture compiler errors if we include the generic type <?>. Instead, we
     * will masquerade all agents as Resource typed instead of the more specific subclass.
     */
    @SuppressWarnings("unchecked")
    WitnessProtectionAgent<Resource> agent =
        (WitnessProtectionAgent<Resource>) agents.get(resource.getClass());
    if (agent == null) {
      log.warn("Witness protection agent not found for {}", resource.getClass());
      return;
    }
    Operations<Resource, ProtectedReference> operations =
        Operations.<Resource, ProtectedReference>builder()
            .toReferences(
                rsrc ->
                    concat(additionalReferences.stream(), agent.referencesOf(rsrc))
                        .filter(Objects::nonNull))
            .isReplaceable(reference -> true)
            .resourceNameOf(ProtectedReference::type)
            .transform(identityProcessor.transformation())
            .build();
    identityProcessor.replace().accept(resource, operations);
  }

  private void processResource(
      @NonNull Resource resource,
      @NonNull WitnessProtectionAdvice.IdentityProcessor identityProcessor) {
    processResource(resource, List.of(), identityProcessor);
  }

  private List<ResourceIdentity> safeLookup(String publicId) {
    try {
      return identityService.lookup(publicId);
    } catch (IdEncoder.BadId e) {
      return emptyList();
    }
  }

  @Override
  public boolean supports(
      MethodParameter returnType,
      @org.springframework.lang.NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    return AbstractBundle.class.isAssignableFrom(returnType.getParameterType())
        || Resource.class.isAssignableFrom(returnType.getParameterType());
  }

  @Override
  public boolean supports(
      @org.springframework.lang.NonNull MethodParameter methodParameter,
      @org.springframework.lang.NonNull Type type,
      @org.springframework.lang.NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    return supports(methodParameter, converterType);
  }

  @Override
  public String toPrivateId(String publicId) {
    try {
      return identityService.lookup(publicId).stream()
          .map(ResourceIdentity::identifier)
          .findFirst()
          .orElse(publicId);
    } catch (BadId e) {
      /*
       * When clear ID support is enabled, we still to honor incoming encrypted IDs. If we fail to
       * decrypted them, they assume they were already clear. If clear ID support is disabled, then
       * this ID is hot garbage.
       */
      if (clearIds) {
        return publicId;
      }
      throw e;
    }
  }

  @Override
  public <R extends Resource> String toPublicId(Class<R> resourceType, String privateId) {
    return identityService
        .register(
            List.of(
                ProtectedReference.builder()
                    .type(protectedReferenceFactory.resourceTypeFor(resourceType))
                    .id(privateId)
                    .build()
                    .asResourceIdentity()
                    .get()))
        .stream()
        .map(Registration::uuid)
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("Could not generate private id for resource."));
  }

  /**
   * OMG I HATE YOU SO MUCH. THIS IS A DUMB HACK TO HANDLE THE TRANSITION CASE BETWEEN SUPPORTING
   * ENCODED AND CLEAR IDS. THIS WILL BE DELETED ONCE WE FULLY TRANSITION TO CLEAR.
   */
  @RequiredArgsConstructor
  private static class ClearIdentityService implements IdentityService {
    private static final Pattern FHIR_ID_PATTERN = Pattern.compile(Fhir.ID);

    private final IdentityService backwardCompatible;

    private boolean isPacked(String id) {
      /*
       * oh lawdy ... there are two ways of producing IDs... some are packed
       * SegmentedVistaIdentifiers. So are just random strings with non-FHIR compliant characters.
       *
       * I hate this class, too.
       */
      if (!FHIR_ID_PATTERN.matcher(id).matches()) {
        /*
         * It's a little cheaper to see if the ID is compliant. If it's not, then we know it's not
         * packed. If is, then it _may_ be packed.
         */
        return false;
      }
      try {
        SegmentedVistaIdentifier.unpack(id);
        return true;
      } catch (IllegalArgumentException e) {
        return false;
      }
    }

    @Override
    public List<ResourceIdentity> lookup(String id) {
      if (id.startsWith("I3-")) {
        return backwardCompatible.lookup(id);
      }
      return List.of(
          ResourceIdentity.builder()
              .system("CLEAR")
              .resource("UNKNOWN")
              .identifier(restoreIfNotUnpackable(id))
              .build());
    }

    @Override
    public List<Registration> register(List<ResourceIdentity> identities) {
      return identities.stream()
          .map(
              i ->
                  Registration.builder()
                      .uuid(
                          isPacked(i.identifier())
                              ? i.identifier()
                              : FhirCompliantCharacters.encodeNonCompliantCharacters(
                                  i.identifier()))
                      .resourceIdentities(List.of(i))
                      .build())
          .toList();
    }

    private String restoreIfNotUnpackable(String id) {
      return isPacked(id) ? id : FhirCompliantCharacters.decodeNonCompliantCharacters(id);
    }
  }

  @Value
  @Builder
  static class IdentityProcessor {
    IsTransformable<ProtectedReference> transformation;

    BiConsumer<Resource, Operations<Resource, ProtectedReference>> replace;
  }
}
