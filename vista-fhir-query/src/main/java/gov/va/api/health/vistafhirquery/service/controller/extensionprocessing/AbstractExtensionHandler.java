package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractExtensionHandler implements ExtensionHandler {

  @Getter private final String definingUrl;

  @Getter private final Required required;

  @Getter private final WriteableFilemanValueFactory filemanFactory;
}
