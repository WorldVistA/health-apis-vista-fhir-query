package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.IdentitySubstitution.IdentityMapping;
import gov.va.api.health.ids.api.PrivateToPublicIdTransformation;
import gov.va.api.health.ids.api.PublicToPrivateIdTransformation;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtectionAdvice.IdentityProcessor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

@ExtendWith(MockitoExtension.class)
class WitnessProtectionAdviceTest {
  @Mock IdentityService identityService;

  @Test
  void bundleOfMixedResourcesIsProtected() {
    var f11 = FugaziOne.builder().id("private-f11").build();
    var f12 = FugaziOne.builder().id("private-f12").build();
    var f21 = FugaziTwo.builder().id("private-f21").build();
    var f22 = FugaziTwo.builder().id("private-f22").build();
    FugaziEntry ef11 = new FugaziEntry(f11);
    FugaziEntry ef12 = new FugaziEntry(f12);
    FugaziEntry ef21 = new FugaziEntry(f21);
    FugaziEntry ef22 = new FugaziEntry(f22);
    FugaziBundle bundle = new FugaziBundle(List.of(ef11, ef12, ef21, ef22));
    when(identityService.register(any()))
        .thenReturn(
            List.of(
                registration("FugaziOne", "f11"),
                registration("FugaziOne", "f12"),
                registration("FugaziTwo", "f21"),
                registration("FugaziTwo", "f22")));
    wp().beforeBodyWrite(
            bundle,
            mock(MethodParameter.class),
            mock(MediaType.class),
            StringHttpMessageConverter.class,
            mock(ServerHttpRequest.class),
            mock(ServerHttpResponse.class));
    assertThat(f11.id()).isEqualTo("public-f11");
    assertThat(ef11.fullUrl()).isEqualTo("http://fugazi.com/fugazi/FugaziOne/public-f11");
    assertThat(f12.id()).isEqualTo("public-f12");
    assertThat(ef12.fullUrl()).isEqualTo("http://fugazi.com/fugazi/FugaziOne/public-f12");
    assertThat(f21.id()).isEqualTo("public-f21");
    assertThat(ef21.fullUrl()).isEqualTo("http://fugazi.com/fugazi/FugaziTwo/public-f21");
    assertThat(f22.id()).isEqualTo("public-f22");
    assertThat(ef22.fullUrl()).isEqualTo("http://fugazi.com/fugazi/FugaziTwo/public-f22");
  }

  @Test
  void bundleOfMixedResourcesIsRevealed() {
    List<FugaziEntry> entries = new ArrayList<>();
    Map<FugaziEntry, String> expected = new HashMap<>();
    for (int i = 0; i < 2; i++) {
      var f1 = FugaziOne.builder().id("public-f1" + i).build();
      var f2 = FugaziTwo.builder().id("public-f2" + i).build();
      var e1 = new FugaziEntry(f1);
      var e2 = new FugaziEntry(f2);
      entries.addAll(List.of(e1, e2));
      when(identityService.lookup(eq("public-f1" + i)))
          .thenReturn(List.of(identity("FugaziOne", "f1" + i)));
      expected.put(e1, "private-f1" + i);
      when(identityService.lookup(eq("public-f2" + i)))
          .thenReturn(List.of(identity("FugaziTwo", "f2" + i)));
      expected.put(e2, "private-f2" + i);
    }
    FugaziBundle bundle = new FugaziBundle(entries);
    wp().afterBodyRead(
            bundle,
            mock(HttpInputMessage.class),
            mock(MethodParameter.class),
            mock(Type.class),
            StringHttpMessageConverter.class);
    expected.forEach(
        (entry, expectedId) -> {
          assertThat(entry.resource().id()).isEqualTo(expectedId);
          assertThat(entry.fullUrl()).endsWith(expectedId);
        });
  }

  void forEachIdProcessor(Consumer<IdentityProcessor> assertion) {
    Stream.of(toPrivateIdProcessor(), toPublicIdProcessor()).forEach(assertion);
  }

  private ResourceIdentity identity(String resource, String baseId) {
    return ResourceIdentity.builder()
        .system("VISTA")
        .resource(resource)
        .identifier("private-" + baseId)
        .build();
  }

  @Test
  void invalidResourceThrows() {
    ProtectedReferenceFactory prf = new ProtectedReferenceFactory(linkProperties());
    var wpa =
        WitnessProtectionAdvice.builder()
            .identityService(identityService)
            .availableAgents(List.of())
            .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
            .protectedReferenceFactory(prf)
            .build();
    when(identityService.lookup("public-1")).thenReturn(List.of(identity("FugaziOne", "1")));
    assertThatExceptionOfType(ResourceExceptions.ExpectationFailed.class)
        .isThrownBy(() -> wpa.privateIdForResourceOrDie("public-1", FugaziTwo.class));
  }

  private LinkProperties linkProperties() {
    return LinkProperties.builder()
        .publicUrl("http://awesome.com/fuego")
        .publicR4BasePath("r4")
        .defaultPageSize(99)
        .maxPageSize(99)
        .build();
  }

  private Registration registration(String resource, String baseId) {
    return Registration.builder()
        .uuid("public-" + baseId)
        .resourceIdentities(List.of(identity(resource, baseId)))
        .build();
  }

  @Test
  void singleResourceIsProtected() {
    when(identityService.register(any())).thenReturn(List.of(registration("FugaziOne", "f1")));
    var f1 = FugaziOne.builder().id("private-f1").build();
    wp().beforeBodyWrite(
            f1,
            mock(MethodParameter.class),
            mock(MediaType.class),
            StringHttpMessageConverter.class,
            mock(ServerHttpRequest.class),
            mock(ServerHttpResponse.class));
    assertThat(f1.id()).isEqualTo("public-f1");
    when(identityService.register(any())).thenReturn(List.of(registration("FugaziTwo", "f2")));
    var f2 = FugaziTwo.builder().id("private-f2").build();
    wp().beforeBodyWrite(
            f2,
            mock(MethodParameter.class),
            mock(MediaType.class),
            StringHttpMessageConverter.class,
            mock(ServerHttpRequest.class),
            mock(ServerHttpResponse.class));
    assertThat(f2.id()).isEqualTo("public-f2");
  }

  @Test
  void singleResourceIsRevealed() {
    when(identityService.lookup(eq("public-f1"))).thenReturn(List.of(identity("FugaziOne", "f1")));
    var f1 = FugaziOne.builder().id("public-f1").build();
    wp().afterBodyRead(
            f1,
            mock(HttpInputMessage.class),
            mock(MethodParameter.class),
            mock(Type.class),
            StringHttpMessageConverter.class);
    assertThat(f1.id()).isEqualTo("private-f1");
    when(identityService.lookup(eq("public-f2"))).thenReturn(List.of(identity("FugaziTwo", "f2")));
    var f2 = FugaziTwo.builder().id("public-f2").build();
    wp().afterBodyRead(
            f2,
            mock(HttpInputMessage.class),
            mock(MethodParameter.class),
            mock(Type.class),
            StringHttpMessageConverter.class);
    assertThat(f2.id()).isEqualTo("private-f2");
  }

  private IdentityProcessor toPrivateIdProcessor() {
    return IdentityProcessor.builder()
        .transformation(
            PublicToPrivateIdTransformation.<ProtectedReference>builder()
                .publicIdOf(ProtectedReference::id)
                .updatePublicIdToPrivateId(ProtectedReference::updateId)
                .build())
        .replace(
            (resource, operations) -> {
              IdentityMapping identities =
                  wp().lookup(List.of(resource), operations.toReferences());
              identities.replacePublicIdsWithPrivateIds(List.of(resource), operations);
            })
        .build();
  }

  @Test
  void toPrivateIdReturnsPrivateIdDuh() {
    when(identityService.lookup("public-f1")).thenReturn(List.of(identity("WHATEVER", "f1")));
    assertThat(wp().toPrivateId("public-f1")).isEqualTo("private-f1");
    assertThat(wp().toPrivateId("unknown-f1")).isEqualTo("unknown-f1");
  }

  private IdentityProcessor toPublicIdProcessor() {
    return IdentityProcessor.builder()
        .transformation(
            PrivateToPublicIdTransformation.<ProtectedReference>builder()
                .privateIdOf(ProtectedReference::id)
                .updatePrivateIdToPublicId(ProtectedReference::updateId)
                .build())
        .replace(
            (resource, operations) -> {
              IdentityMapping identities =
                  wp().register(List.of(resource), operations.toReferences());
              identities.replacePrivateIdsWithPublicIds(List.of(resource), operations);
            })
        .build();
  }

  @Test
  void toPublicIdReturnsPublicIdDuh() {
    when(identityService.register(List.of(identity("FugaziOne", "f1"))))
        .thenReturn(List.of(registration("ignored", "f1")));
    assertThat(wp().toPublicId(FugaziOne.class, "private-f1")).isEqualTo("public-f1");
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> wp().toPublicId(FugaziOne.class, "unknown-f1"));
  }

  @Test
  void unknownResourceTypeIsNotModified() {
    ProtectedReferenceFactory prf = new ProtectedReferenceFactory(linkProperties());
    var f1 = FugaziOne.builder().id("private-f1").build();
    var noF1 =
        WitnessProtectionAdvice.builder()
            .identityService(identityService)
            .availableAgents(List.of(FugaziTwoAgent.of(prf)))
            .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
            .protectedReferenceFactory(prf)
            .build();
    forEachIdProcessor(
        o -> {
          noF1.process(f1, o);
          assertThat(f1.id()).isEqualTo("private-f1");
        });
  }

  @Test
  void unregisteredResourcesAreNotModified() {
    var f1 = FugaziOne.builder().id("private-f1").build();
    forEachIdProcessor(
        o -> {
          wp().process(f1, o);
          assertThat(f1.id()).isEqualTo("private-f1");
        });
  }

  @Test
  void validResourceReturnsPrivateId() {
    ProtectedReferenceFactory prf = new ProtectedReferenceFactory(linkProperties());
    var wpa =
        WitnessProtectionAdvice.builder()
            .identityService(identityService)
            .availableAgents(List.of())
            .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
            .protectedReferenceFactory(prf)
            .build();
    when(identityService.lookup("public-1")).thenReturn(List.of(identity("FugaziOne", "1")));
    assertThat(wpa.privateIdForResourceOrDie("public-1", FugaziOne.class)).isEqualTo("private-1");
  }

  private WitnessProtectionAdvice wp() {
    ProtectedReferenceFactory prf = new ProtectedReferenceFactory(linkProperties());
    return WitnessProtectionAdvice.builder()
        .protectedReferenceFactory(prf)
        .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
        .identityService(identityService)
        .availableAgents(List.of(FugaziOneAgent.of(prf), FugaziTwoAgent.of(prf)))
        .build();
  }

  static class FugaziBundle extends AbstractBundle<FugaziEntry> {
    FugaziBundle(List<FugaziEntry> e) {
      entry(e);
    }
  }

  static class FugaziEntry extends AbstractEntry<Resource> {
    FugaziEntry(Resource r) {
      resource(r);
      fullUrl("http://fugazi.com/fugazi/" + r.getClass().getSimpleName() + "/" + r.id());
    }
  }

  @Data
  @Builder
  static class FugaziOne implements Resource {
    String id;

    String implicitRules;

    String language;

    Meta meta;
  }

  @AllArgsConstructor(staticName = "of")
  static class FugaziOneAgent implements WitnessProtectionAgent<FugaziOne> {
    ProtectedReferenceFactory prf;

    @Override
    public Stream<ProtectedReference> referencesOf(FugaziOne resource) {
      return Stream.of(prf.forResource(resource, resource::id));
    }
  }

  @Data
  @Builder
  static class FugaziTwo implements Resource {
    String id;

    String implicitRules;

    String language;

    Meta meta;
  }

  @AllArgsConstructor(staticName = "of")
  static class FugaziTwoAgent implements WitnessProtectionAgent<FugaziTwo> {
    ProtectedReferenceFactory prf;

    @Override
    public Stream<ProtectedReference> referencesOf(FugaziTwo resource) {
      return Stream.of(prf.forResource(resource, resource::id));
    }
  }
}
