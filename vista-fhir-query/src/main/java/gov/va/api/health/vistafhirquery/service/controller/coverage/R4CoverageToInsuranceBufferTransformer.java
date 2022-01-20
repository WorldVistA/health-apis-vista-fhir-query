package gov.va.api.health.vistafhirquery.service.controller.coverage;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.controller.FilemanFactoryRegistry;
import gov.va.api.health.vistafhirquery.service.controller.FilemanIndexRegistry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageToInsuranceBufferTransformer {
  FilemanFactoryRegistry factoryRegistry;

  FilemanIndexRegistry indexRegistry;

  @NonNull Coverage coverage;

  @Builder
  R4CoverageToInsuranceBufferTransformer(@NonNull Coverage coverage) {
    this.coverage = coverage;
    this.factoryRegistry = FilemanFactoryRegistry.create();
    this.indexRegistry = FilemanIndexRegistry.create();
  }

  WriteableFilemanValue status() {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.STATUS,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            "E")
        .get();
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceBuffer() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    fields.add(status());
    return fields;
  }
}
