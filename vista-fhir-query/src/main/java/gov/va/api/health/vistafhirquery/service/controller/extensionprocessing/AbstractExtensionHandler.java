package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import lombok.Getter;

public abstract class AbstractExtensionHandler implements ExtensionHandler {

  @Getter private final String definingUrl;

  @Getter private final Required required;

  @Getter private final WriteableFilemanValueFactory filemanFactory;

  @Getter private final int index;

  protected AbstractExtensionHandler(
      String definingUrl,
      Required required,
      WriteableFilemanValueFactory filemanFactory,
      int index) {
    this.definingUrl = definingUrl;
    this.required = required;
    this.filemanFactory = filemanFactory;
    this.index = index;
  }
}
