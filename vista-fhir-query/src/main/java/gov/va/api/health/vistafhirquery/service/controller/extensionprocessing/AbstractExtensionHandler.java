package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractExtensionHandler implements ExtensionHandler {

  @Getter private final String definingUrl;

  @Getter private final IsRequired required;

  @Getter private final String fieldNumber;

  @Getter private final WriteableFilemanValueFactory filemanFactory;

  public enum IsRequired {
    REQUIRED,
    OPTIONAL
  }
}
