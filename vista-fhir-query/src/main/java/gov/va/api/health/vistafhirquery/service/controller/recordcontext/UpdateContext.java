package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.IsSiteCoordinates;

public interface UpdateContext<BodyT extends IsResource> extends WriteContext<BodyT> {

  IsSiteCoordinates existingRecord();

  String existingRecordPublicId();
}
