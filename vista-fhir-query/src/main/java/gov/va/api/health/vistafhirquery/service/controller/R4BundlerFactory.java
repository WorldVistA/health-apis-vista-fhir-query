package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler.R4BundlerBuilder;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** R4BundlerFactory. */
@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
@Builder
public class R4BundlerFactory {

  @Getter @NonNull private final LinkProperties linkProperties;
  @Getter @NonNull private final AlternatePatientIds alternatePatientIds;

  /** Create a new instance for the given transformation. */
  public <RpcResponseT, ResourceT extends Resource>
      R4BundlerPart1<RpcResponseT, ResourceT> forTransformation(
          R4Transformation<RpcResponseT, ResourceT> transformation) {
    return R4BundlerPart1.<RpcResponseT, ResourceT>builder()
        .fromFactory(this)
        .transformation(transformation)
        .build();
  }

  /**
   * These builder parts are used to slowly infer the generics types based on the arguments vs.
   * specifying the types and requires arguments that match.
   */
  @Builder
  public static class R4BundlerPart1<V, R extends Resource> {

    private final R4BundlerFactory fromFactory;
    private final R4Transformation<V, R> transformation;

    public R4BundlerPart2<V, R> site(@NonNull String site) {
      return R4BundlerPart2.<V, R>builder().part1(this).site(site).build();
    }

    public R4BundlerPart2<V, R> withoutSite() {
      return R4BundlerPart2.<V, R>builder().part1(this).site(null).build();
    }
  }

  /**
   * These builder parts are used to slowly infer the generics types based on the arguments vs.
   * specifying the types and requires arguments that match.
   */
  @Builder
  public static class R4BundlerPart2<V, R extends Resource> {

    private final R4BundlerPart1<V, R> part1;
    private final String site;

    /** Create the next phrase after configuring link properties and alternate patient ids. */
    public <E extends AbstractEntry<R>, B extends AbstractBundle<E>>
        R4BundlerBuilder<V, R, E, B> bundling(R4Bundling<R, E, B> bundling) {
      return R4Bundler.<V, R, E, B>builder()
          .linkProperties(part1.fromFactory.linkProperties())
          .alternatePatientIds(part1.fromFactory.alternatePatientIds())
          .transformation(part1.transformation)
          .site(site)
          .bundling(bundling);
    }
  }
}
