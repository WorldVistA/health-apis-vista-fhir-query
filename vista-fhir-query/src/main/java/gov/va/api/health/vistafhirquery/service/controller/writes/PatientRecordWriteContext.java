package gov.va.api.health.vistafhirquery.service.controller.writes;

import gov.va.api.health.fhir.api.IsResource;

public interface PatientRecordWriteContext<BodyT extends IsResource> extends WriteContext<BodyT> {
  String patientIcn();
}
