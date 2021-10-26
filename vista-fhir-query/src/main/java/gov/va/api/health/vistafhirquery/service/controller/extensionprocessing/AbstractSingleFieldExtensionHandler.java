package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import lombok.Getter;

public abstract class AbstractSingleFieldExtensionHandler extends AbstractExtensionHandler {
  @Getter private final String fieldNumber;

  protected AbstractSingleFieldExtensionHandler(
      String definingUrl,
      Required required,
      WriteableFilemanValueFactory filemanFactory,
      String fieldNumber) {
    super(definingUrl, required, filemanFactory);
    this.fieldNumber = fieldNumber;
  }
}
