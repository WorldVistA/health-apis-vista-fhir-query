package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toLocalDateMacroString;
import static gov.va.api.health.vistafhirquery.service.controller.appointment.AppointmentSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonRequest;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonResponse;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class R4SiteAppointmentControllerTest {
  @Mock CharonClient charonClient;
  @Mock VistaApiConfig vistaApiConfig;

  private R4SiteAppointmentController _controller() {
    return R4SiteAppointmentController.builder()
        .bundlerFactory(
            R4BundlerFactory.builder()
                .linkProperties(
                    LinkProperties.builder()
                        .defaultPageSize(15)
                        .maxPageSize(100)
                        .publicUrl("http://fugazi.com")
                        .publicR4BasePath("hcs/{site}/r4")
                        .build())
                .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
                .build())
        .charonClient(charonClient)
        .vistaApiConfig(vistaApiConfig)
        .build();
  }

  private RpcInvocationResultV1 _invocationResult(Object value) {
    return RpcInvocationResultV1.builder()
        .vista("673")
        .timezone("-0500")
        .response(json(value))
        .build();
  }

  private <I extends TypeSafeRpcRequest>
      CharonRequest<I, VprGetPatientData.Response.Results> charonRequestFor(I request) {
    return CharonRequest.<I, VprGetPatientData.Response.Results>builder()
        .vista("673")
        .rpcRequest(request)
        .responseType(VprGetPatientData.Response.Results.class)
        .build();
  }

  private <I extends TypeSafeRpcRequest>
      CharonResponse<I, VprGetPatientData.Response.Results> charonResponseFor(
          CharonRequest<I, VprGetPatientData.Response.Results> request,
          VprGetPatientData.Response.Results results) {
    return CharonResponse.<I, VprGetPatientData.Response.Results>builder()
        .request(request)
        .invocationResult(_invocationResult(results))
        .value(results)
        .build();
  }

  @Test
  void searchByPatient() {
    var httpRequest = requestFromUri("?_count=15&patient=p1");

    String[] date = new String[] {"1991"};

    DateSearchBoundaries boundaries = DateSearchBoundaries.of(date);

    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(VprGetPatientData.Request.PatientId.forIcn("p1"))
            .type(Set.of(VprGetPatientData.Domains.appointments))
            .start(toLocalDateMacroString(boundaries.start()))
            .stop(toLocalDateMacroString(boundaries.stop()))
            .build();
    var charonRequest = charonRequestFor(rpcRequest);
    var charonResponse = AppointmentSamples.Vista.create().results();

    when(charonClient.request(any(CharonRequest.class)))
        .thenReturn(charonResponseFor(charonRequest, charonResponse));

    var actual = _controller().appointmentSearch(httpRequest, "673", date, "p1", 15);
    var expected =
        AppointmentSamples.R4.asBundle(
            "http://fugazi.com/hcs/673/r4",
            List.of(AppointmentSamples.R4.create().appointment()),
            1,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/673/r4/Appointment",
                "_count=15&patient=p1"));

    assertThat(json(actual)).isEqualTo(json(expected));
  }
}
