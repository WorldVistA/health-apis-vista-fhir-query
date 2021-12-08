package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;

public interface ExtensionHandler {
  String definingUrl();

  List<WriteableFilemanValue> handle(String jsonPath, Extension extension);

  Required required();

  enum Required {
    REQUIRED,
    OPTIONAL
  }
}
