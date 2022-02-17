package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
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
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.medicationdispense.MedicationDispenseSamples.Vista;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds.DisabledAlternatePatientIds;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Request.PatientId;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Response.Results;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class R4SiteMedicationDispenseControllerTest {

  @Mock CharonClient charonClient;

  @Mock VistaApiConfig vistaApiConfig;

  MockWitnessProtection witnessProtection = new MockWitnessProtection();

  private <I extends TypeSafeRpcRequest> CharonRequest<I, Results> _charonRequestFor(I request) {
    return CharonRequest.<I, VprGetPatientData.Response.Results>builder()
        .vista("673")
        .rpcRequest(request)
        .responseType(VprGetPatientData.Response.Results.class)
        .build();
  }

  private <I extends TypeSafeRpcRequest> CharonResponse<I, Results> _charonResponseFor(
      CharonRequest<I, VprGetPatientData.Response.Results> request,
      VprGetPatientData.Response.Results results) {
    return CharonResponse.<I, VprGetPatientData.Response.Results>builder()
        .request(request)
        .invocationResult(_invocationResult(results))
        .value(results)
        .build();
  }

  private R4SiteMedicationDispenseController _controller() {
    return R4SiteMedicationDispenseController.builder()
        .bundlerFactory(
            R4BundlerFactory.builder()
                .linkProperties(
                    LinkProperties.builder()
                        .defaultPageSize(15)
                        .maxPageSize(100)
                        .publicUrl("http://fugazi.com")
                        .publicR4BasePath("hcs/{site}/r4")
                        .build())
                .alternatePatientIds(new DisabledAlternatePatientIds())
                .build())
        .charonClient(charonClient)
        .vistaApiConfig(vistaApiConfig)
        .witnessProtection(witnessProtection)
        .build();
  }

  private RpcInvocationResultV1 _invocationResult(Object value) {
    return RpcInvocationResultV1.builder()
        .vista("673")
        .timezone("-0500")
        .response(json(value))
        .build();
  }

  @Test
  void read() {
    var httpRequest = requestFromUri("?patient=p1");
    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(PatientId.forIcn("I3-p1"))
            .type(Set.of(Domains.meds))
            .build();
    var charonRequest = _charonRequestFor(rpcRequest);
    var charonResponse = MedicationDispenseSamples.Vista.create().results();
    when(charonClient.request(any(CharonRequest.class)))
        .thenReturn(_charonResponseFor(charonRequest, charonResponse));
    witnessProtection.add("p1", "sNp1+673+M33714:3110507");
    var actual = _controller().medicationDispenseRead(httpRequest, "673", "p1");
    assertThat(json(actual))
        .isEqualTo(json(MedicationDispenseSamples.R4.create().medicationDispense()));
  }

  @Test
  void searchByPatient() {
    var httpRequest = requestFromUri("?_count=15&patient=p1");
    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(PatientId.forIcn("p1"))
            .type(Set.of(Domains.meds))
            .build();
    var charonRequest = _charonRequestFor(rpcRequest);
    var charonResponse = MedicationDispenseSamples.Vista.create().results();
    when(charonClient.request(any(CharonRequest.class)))
        .thenReturn(_charonResponseFor(charonRequest, charonResponse));
    var actual = _controller().medicationDispenseSearch(httpRequest, "673", "p1", null, 15);
    var expected =
        MedicationDispenseSamples.R4.asBundle(
            "http://fugazi.com/hcs/673/r4",
            List.of(MedicationDispenseSamples.R4.create().medicationDispense()),
            1,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/673/r4/MedicationDispense",
                "_count=15&patient=p1"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchByPatientWithDate() {
    var httpRequest = requestFromUri("?date=gt2020&patient=p1");
    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(PatientId.forIcn("p1"))
            .type(Set.of(Domains.meds))
            .build();
    var charonRequest = _charonRequestFor(rpcRequest);
    var vista = Vista.create();
    var charonResponse =
        vista.results(
            vista.med(
                "1",
                vista.fill("3040121"),
                vista.fill("3050121"),
                vista.fill("3060121"),
                vista.fill("3070121")));
    when(charonClient.request(any(CharonRequest.class)))
        .thenReturn(_charonResponseFor(charonRequest, charonResponse));

    String[] anytime = {};
    String[] gt2004 = {"gt2004"};
    String[] gt2004lt2007 = {"gt2004", "lt2007"};
    String[] zombieApocalypse = {"eq2022"};

    Function<String[], Integer> totalFor =
        dates ->
            _controller().medicationDispenseSearch(httpRequest, "673", "p1", dates, 15).total();

    assertThat(totalFor.apply(null)).isEqualTo(4);
    assertThat(totalFor.apply(anytime)).isEqualTo(4);
    assertThat(totalFor.apply(gt2004)).isEqualTo(3);
    assertThat(totalFor.apply(gt2004lt2007)).isEqualTo(2);
    assertThat(totalFor.apply(zombieApocalypse)).isEqualTo(0);
  }
}
