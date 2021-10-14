package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;

import gov.va.api.health.r4.api.elements.Extension;
import java.util.List;

public interface ExtensionProcessor {

  List<WriteableFilemanValue> process(List<Extension> extensions);
}
