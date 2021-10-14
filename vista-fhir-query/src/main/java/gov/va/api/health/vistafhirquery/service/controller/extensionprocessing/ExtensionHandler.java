package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.AbstractExtensionHandler.IsRequired;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;

public interface ExtensionHandler {
  String definingUrl();

  String fieldNumber();

  WriteableFilemanValueFactory filemanFactory();

  WriteableFilemanValue handle(Extension extension);

  IsRequired required();
}
