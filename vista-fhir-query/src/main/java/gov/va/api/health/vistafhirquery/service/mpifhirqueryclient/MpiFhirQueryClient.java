package gov.va.api.health.vistafhirquery.service.mpifhirqueryclient;

import java.util.Set;

/** Interact with the MPI Fhir Query server. */
public interface MpiFhirQueryClient {
  Set<String> stationIdsForPatient(String patient);
}
