package gov.va.api.health.vistafhirquery.service.charonclient;

import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class FilemanEntries {
  public static final String SUCCESS = "1";

  public static boolean isFailure(FilemanEntry entry) {
    return !isSuccessful(entry);
  }

  public static boolean isSuccessful(FilemanEntry entry) {
    return SUCCESS.equals(entry.status());
  }

  /** Logs if there is a failure for a lookup on a FilemanEntry. */
  public static FilemanEntry logIfFailure(FilemanEntry entry) {
    if (isFailure(entry)) {
      log.warn("File {} with IEN {} has status of {}", entry.file(), entry.ien(), entry.status());
    }
    return entry;
  }
}
