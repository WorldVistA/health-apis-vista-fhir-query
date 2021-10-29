package gov.va.api.health.vistafhirquery.service.controller;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import gov.va.api.health.vistafhirquery.service.charonclient.FilemanEntries;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@AllArgsConstructor
public class FileLookup {

  private final Map<FileEntry, FilemanEntry> subfiles;

  /** Get a FileLookup that operates on a LhsLighthouseRpcGatewayResponse.Results. */
  public static FileLookup of(LhsLighthouseRpcGatewayResponse.Results results) {
    return new FileLookup(
        results.results().stream()
            .peek(FilemanEntries::logIfFailure)
            .filter(FilemanEntries::isSuccessful)
            .collect(toMap(e -> FileEntry.of(e.ien(), e.file()), identity())));
  }

  /**
   * Find all subfiles belonging to the parent ien matching the subfileNumber.
   *
   * <p>If parent file is 36 with an IEN of 7, then the subfile will have an IEN that ends with ","
   * + parent IEN, e.g. 36.012 with IEN 1,7,
   *
   * <p>IENS tend to have a trailing comma, but that doesn't really matter here.
   */
  public List<FilemanEntry> findByFileNumberAndParentIen(
      @NonNull String subfileNumber, @NonNull String parentIen) {
    return subfiles.entrySet().stream()
        .filter(e -> subfileNumber.equals(e.getKey().fileNumber()))
        .filter(e -> e.getKey().ien().endsWith("," + parentIen))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());
  }

  @Value
  @AllArgsConstructor(staticName = "of")
  private static class FileEntry {
    @NonNull String ien;
    @NonNull String fileNumber;
  }
}
