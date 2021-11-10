package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Money;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Insurance;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Item;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Purpose;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.EligibilityBenefit;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.HealthCareCodeInformation;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.HealthcareServicesDelivery;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.IivResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Values;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.ServiceTypes;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberAdditionalInfo;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberDates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.SubscriberReferenceId;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CoverageEligibilityResponseSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    public static CoverageEligibilityResponse.Bundle asBundle(
        String baseUrl,
        Collection<CoverageEligibilityResponse> resources,
        int totalRecords,
        BundleLink... links) {
      return CoverageEligibilityResponse.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      resource ->
                          CoverageEligibilityResponse.Entry.builder()
                              .fullUrl(baseUrl + "/CoverageEligibilityResponse/" + resource.id())
                              .resource(resource)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static BundleLink link(BundleLink.LinkRelation rel, String base, String query) {
      return BundleLink.builder().relation(rel).url(base + "?" + query).build();
    }

    private List<CoverageEligibilityResponse.Benefit> benefits() {
      return CoverageEligibilityResponse.Benefit.builder()
          .type(
              CodeableConcept.builder()
                  .coding(
                      Coding.builder()
                          .code("1")
                          .system(
                              CoverageEligibilityResponseStructureDefinitions
                                  .ELIGIBILITY_BENEFIT_INFO)
                          .build()
                          .asList())
                  .build())
          .allowedMoney(Money.builder().value(new BigDecimal("250.25")).build())
          .build()
          .asList();
    }

    private CodeableConcept codeableConceptFor(String system, String code) {
      return CodeableConcept.builder()
          .coding(Coding.builder().system(system).code(code).build().asList())
          .build();
    }

    public CoverageEligibilityResponse coverageEligibilityResponse() {
      return coverageEligibilityResponse("p1", "8", "4", "1,8,");
    }

    public CoverageEligibilityResponse coverageEligibilityResponse(
        String patient, String limitation, String organization, String coverage) {
      return CoverageEligibilityResponse.builder()
          .meta(Meta.builder().source("123").build())
          .id(
              PatientTypeCoordinates.builder()
                  .site("123")
                  .icn(patient)
                  .ien(coverage)
                  .build()
                  .toString())
          .status(CoverageEligibilityResponse.Status.active)
          .purpose(List.of(Purpose.benefits, Purpose.discovery))
          .patient(Reference.builder().reference("Patient/" + patient).build())
          .created("ignored")
          ._request(DataAbsentReason.of(DataAbsentReason.Reason.unsupported))
          .outcome(CoverageEligibilityResponse.Outcome.complete)
          .insurer(
              Reference.builder()
                  .reference(
                      "Organization/"
                          + RecordCoordinates.builder()
                              .site("123")
                              .file(InsuranceCompany.FILE_NUMBER)
                              .ien(organization)
                              .build()
                              .toString())
                  .build())
          .insurance(List.of(insurance(patient, coverage)))
          .build();
    }

    public CoverageEligibilityResponse coverageEligibilityResponseForWrite() {
      return coverageEligibilityResponseForWrite("123", "p1", "1,8,");
    }

    public CoverageEligibilityResponse coverageEligibilityResponseForWrite(
        String site, String patient, String coverage) {
      return CoverageEligibilityResponse.builder()
          .meta(Meta.builder().source("123").build())
          .id(
              PatientTypeCoordinates.builder()
                  .site(site)
                  .icn(patient)
                  .ien(coverage)
                  .build()
                  .toString())
          .patient(Reference.builder().reference("Patient/" + patient).build())
          .identifier(
              List.of(
                  Identifier.builder()
                      .type(CodeableConcept.builder().text("MSH-10").build())
                      .value("MCI-1234")
                      .build(),
                  Identifier.builder()
                      .type(CodeableConcept.builder().text("MSA-3").build())
                      .value("TN-1234")
                      .build()))
          .insurer(
              Reference.builder()
                  .reference(
                      "Organization/"
                          + RecordCoordinates.builder()
                              .file(Payer.FILE_NUMBER)
                              .site(site)
                              .ien("8")
                              .build())
                  .build())
          .extension(extensions())
          .servicedDate("2010-01-20")
          .insurance(
              Insurance.builder()
                  .coverage(
                      Reference.builder()
                          .reference(
                              "Coverage/"
                                  + PatientTypeCoordinates.builder()
                                      .site(site)
                                      .icn(patient)
                                      .ien(coverage)
                                      .build()
                                      .toString())
                          .build())
                  .benefitPeriod(Period.builder().start("2004-02-03").end("2007-05-06").build())
                  .item(
                      Item.builder()
                          .extension(itemExtensions())
                          .category(
                              codeableConceptFor(
                                  CoverageEligibilityResponseStructureDefinitions.SERVICE_TYPES,
                                  "OTHER MEDICAL"))
                          .benefit(benefits())
                          .modifier(
                              codeableConceptFor(
                                      CoverageEligibilityResponseStructureDefinitions.ITEM_MODIFIER,
                                      "Modified")
                                  .asList())
                          .term(
                              codeableConceptFor(
                                  CoverageEligibilityResponseStructureDefinitions.ITEM_TERM, "7"))
                          .unit(
                              codeableConceptFor(
                                  CoverageEligibilityResponseStructureDefinitions.ITEM_UNIT, "2"))
                          .productOrService(codeableConceptFor("N4", "IE123"))
                          .network(
                              codeableConceptFor(
                                  CoverageEligibilityResponseStructureDefinitions.X12_YES_NO_SYSTEM,
                                  "N"))
                          .authorizationRequired(true)
                          .build()
                          .asList())
                  .build()
                  .asList())
          .build();
    }

    private Extension createExtension(String definingUrl, Consumer<Extension> populateValueX) {
      var extension = Extension.builder().url(definingUrl).build();
      populateValueX.accept(extension);
      return extension;
    }

    private List<Extension> extensions() {
      return List.of(
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_INFO_STATUS_CODE_DEFINITION)
              .valueCodeableConcept(
                  codeableConceptFor(
                      CoverageEligibilityResponseStructureDefinitions.MILITARY_INFO_STATUS_CODE,
                      "P"))
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_EMPLOYMENT_STATUS_DEFINITION)
              .valueCodeableConcept(
                  codeableConceptFor(
                      CoverageEligibilityResponseStructureDefinitions.MILITARY_EMPLOYMENT_STATUS,
                      "CC"))
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_GOVT_AFFILIATION_CODE_DEFINITION)
              .valueCodeableConcept(
                  codeableConceptFor(
                      CoverageEligibilityResponseStructureDefinitions
                          .MILITARY_GOVT_AFFILIATION_CODE,
                      "C"))
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_PERSONNEL_DESCRIPTION_DEFINITION)
              .valueString("ARMY")
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .MILITARY_SERVICE_RANK_CODE_DEFINITION)
              .valueCodeableConcept(
                  codeableConceptFor(
                      CoverageEligibilityResponseStructureDefinitions.MILITARY_SERVICE_RANK_CODE,
                      "C1"))
              .build(),
          Extension.builder()
              .url(
                  "http://va.gov/fhir/StructureDefinition/coverageEligibilityResponse-healthCareCode")
              .extension(
                  List.of(
                      Extension.builder()
                          .url("diagnosisCode")
                          .valueCodeableConcept(
                              CodeableConcept.builder()
                                  .coding(
                                      Coding.builder()
                                          .system("http://hl7.org/fhir/sid/icd-9-cm")
                                          .code("100.81")
                                          .build()
                                          .asList())
                                  .build())
                          .build(),
                      Extension.builder()
                          .url("diagnosisCodeQualifier")
                          .valueString("ICD-9-CM")
                          .build(),
                      Extension.builder().url("primaryOrSecondary").valueString("PRIMARY").build()))
              .build());
    }

    private Insurance insurance(String patient, String coverage) {
      return Insurance.builder()
          .coverage(
              Reference.builder()
                  .reference(
                      "Coverage/"
                          + PatientTypeCoordinates.builder()
                              .site("123")
                              .icn(patient)
                              .ien(coverage)
                              .build()
                              .toString())
                  .build())
          .inforce(true)
          .benefitPeriod(Period.builder().start("1992-01-12T00:00:00Z").build())
          .item(
              List.of(
                  Item.builder()
                      .category(
                          CodeableConcept.builder()
                              .coding(
                                  List.of(
                                      Coding.builder()
                                          .system(
                                              "urn:oid:2.16.840.1.113883.3.8901.3.1.3558002.8002")
                                          .code("MENTAL HEALTH")
                                          .build()))
                              .build())
                      .excluded(true)
                      .build()))
          .build();
    }

    private List<Extension> itemExtensions() {
      return List.of(
          Extension.builder()
              .url(CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE)
              .extension(
                  List.of(
                      Extension.builder()
                          .url(
                              CoverageEligibilityResponseStructureDefinitions
                                  .SUBSCRIBER_DATE_PERIOD)
                          .valuePeriod(Period.builder().start("2018-05-20").build())
                          .build(),
                      Extension.builder()
                          .url(CoverageEligibilityResponseStructureDefinitions.SUBSCRIBER_DATE_KIND)
                          .valueCodeableConcept(
                              CodeableConcept.builder()
                                  .coding(
                                      List.of(
                                          Coding.builder()
                                              .system(
                                                  CoverageEligibilityResponseStructureDefinitions
                                                      .SUBSCRIBER_DATE_QUALIFIER)
                                              .code("PLAN BEGIN")
                                              .build()))
                                  .build())
                          .build()))
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .SUBSCRIBER_REFERENCE_ID_DEFINITION)
              .extension(
                  List.of(
                      Extension.builder()
                          .url(
                              CoverageEligibilityResponseStructureDefinitions
                                  .SUBSCRIBER_REFERENCE_ID_VALUE_DEFINITION)
                          .valueString("REF123")
                          .build(),
                      Extension.builder()
                          .url(
                              CoverageEligibilityResponseStructureDefinitions
                                  .SUBSCRIBER_REFERENCE_ID_QUALIFIER_DEFINITION)
                          .valueCodeableConcept(
                              CodeableConcept.builder()
                                  .coding(
                                      Coding.builder()
                                          .system(
                                              CoverageEligibilityResponseStructureDefinitions
                                                  .SUBSCRIBER_REFERENCE_ID_QUALIFIER)
                                          .code("18")
                                          .build()
                                          .asList())
                                  .build())
                          .build(),
                      Extension.builder()
                          .url(
                              CoverageEligibilityResponseStructureDefinitions
                                  .SUBSCRIBER_REFERENCE_ID_DESCRIPTION_DEFINITION)
                          .valueString("BCBS")
                          .build()))
              .build(),
          Extension.builder()
              .url(
                  CoverageEligibilityResponseStructureDefinitions
                      .SUBSCRIBER_ADDITIONAL_INFO_DEFINITION)
              .extension(
                  List.of(
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions
                              .SUBSCRIBER_PLACE_OF_SERVICE_DEFINITION,
                          e ->
                              e.valueCodeableConcept(
                                  codeableConceptFor(
                                      CoverageEligibilityResponseStructureDefinitions
                                          .SUBSCRIBER_PLACE_OF_SERVICE_SYSTEM,
                                      "12"))),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions
                              .SUBSCRIBER_QUALIFIER_DEFINITION,
                          e ->
                              e.valueCodeableConcept(
                                  codeableConceptFor(
                                      CoverageEligibilityResponseStructureDefinitions
                                          .SUBSCRIBER_QUALIFIER_SYSTEM,
                                      "NI"))),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions
                              .SUBSCRIBER_INJURY_CODE_DEFINITION,
                          e ->
                              e.valueCodeableConcept(
                                  codeableConceptFor(
                                      CoverageEligibilityResponseStructureDefinitions
                                          .SUBSCRIBER_INJURY_CODE_SYSTEM,
                                      "GR"))),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions
                              .SUBSCRIBER_INJURY_CATEGORY_DEFINITION,
                          e ->
                              e.valueCodeableConcept(
                                  codeableConceptFor(
                                      CoverageEligibilityResponseStructureDefinitions
                                          .SUBSCRIBER_INJURY_CATEGORY_SYSTEM,
                                      "NI"))),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions
                              .SUBSCRIBER_INJURY_TEXT_DEFINITION,
                          e -> e.valueString("ARM IS BORKED"))))
              .build(),
          Extension.builder()
              .url(CoverageEligibilityResponseStructureDefinitions.HEALTHCARE_SERVICES_DELIVERY)
              .extension(
                  List.of(
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions.BENEFIT_QUANTITY,
                          e -> e.valueDecimal(new BigDecimal("365.666"))),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions.QUANTITY_QUALIFIER,
                          e ->
                              e.valueCodeableConcept(
                                  CodeableConcept.builder()
                                      .coding(
                                          Coding.builder()
                                              .system(
                                                  CoverageEligibilityResponseStructureDefinitions
                                                      .QUANTITY_QUALIFIER_SYSTEM)
                                              .code("m2")
                                              .build()
                                              .asList())
                                      .build())),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions.SAMPLE_SELECTION_MODULUS,
                          e -> e.valueString("5")),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions.UNITS_OF_MEASUREMENT,
                          e ->
                              e.valueCodeableConcept(
                                  CodeableConcept.builder()
                                      .coding(
                                          Coding.builder()
                                              .system(
                                                  CoverageEligibilityResponseStructureDefinitions
                                                      .UNITS_OF_MEASUREMENT_SYSTEM)
                                              .code("VS")
                                              .build()
                                              .asList())
                                      .build())),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions.TIME_PERIODS,
                          e -> e.valueInteger(3)),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions.TIME_PERIOD_QUALIFIER,
                          e ->
                              e.valueCodeableConcept(
                                  CodeableConcept.builder()
                                      .coding(
                                          Coding.builder()
                                              .system(
                                                  CoverageEligibilityResponseStructureDefinitions
                                                      .TIME_PERIOD_QUALIFIER_SYSTEM)
                                              .code("32")
                                              .build()
                                              .asList())
                                      .build())),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions.DELIVERY_FREQUENCY,
                          e ->
                              e.valueCodeableConcept(
                                  CodeableConcept.builder()
                                      .coding(
                                          Coding.builder()
                                              .system(
                                                  CoverageEligibilityResponseStructureDefinitions
                                                      .DELIVERY_FREQUENCY_SYSTEM)
                                              .code("N")
                                              .build()
                                              .asList())
                                      .build())),
                      createExtension(
                          CoverageEligibilityResponseStructureDefinitions.DELIVERY_PATTERN,
                          e ->
                              e.valueCodeableConcept(
                                  CodeableConcept.builder()
                                      .coding(
                                          Coding.builder()
                                              .system(
                                                  CoverageEligibilityResponseStructureDefinitions
                                                      .DELIVERY_PATTERN_SYSTEM)
                                              .code("G")
                                              .build()
                                              .asList())
                                      .build()))))
              .build());
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    public List<WriteableFilemanValue> eligibilityBenefitFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.ELIGIBILITY_BENEFIT_INFO)
              .value("1")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.COVERAGE_LEVEL)
              .value("2")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.TIME_PERIOD_QUALIFIER)
              .value("7")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.MONETARY_AMOUNT)
              .value("250.25")
              .build(), /*          WriteableFilemanValue.builder()
                            .file(EligibilityBenefit.FILE_NUMBER)
                            .index(1)
                            .field(EligibilityBenefit.PERCENT)
                            .value("88.88")
                            .build(),
                        WriteableFilemanValue.builder()
                            .file(EligibilityBenefit.FILE_NUMBER)
                            .index(1)
                            .field(EligibilityBenefit.QUANTITY_QUALIFIER)
                            // M2 Maximum
                            .value("M2")
                            .build(),
                        WriteableFilemanValue.builder()
                            .file(EligibilityBenefit.FILE_NUMBER)
                            .index(1)
                            .field(EligibilityBenefit.QUANTITY)
                            .value("666")
                            .build(),*/
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.AUTHORIZATION_CERTIFICATION)
              .value("Y")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.IN_PLAN)
              .value("N")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.PROCEDURE_CODING_METHOD)
              .value("N4")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.PROCEDURE_CODE)
              .value("IE123")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.PROCEDURE_MODIFIER_1)
              .value("Modified")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field(EligibilityBenefit.EB_NUMBER)
              .value("1")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthCareCodeInformation.FILE_NUMBER)
              .index(1)
              .field(HealthCareCodeInformation.SEQUENCE)
              .value("1")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberDates.FILE_NUMBER)
              .index(1)
              .field(SubscriberDates.SEQUENCE)
              .value("1")
              .build());
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults() {
      return getsManifestResults("8");
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  FilemanEntry.builder()
                      .status("1")
                      .file(InsuranceType.FILE_NUMBER)
                      .ien(id)
                      .fields(Map.of())
                      .build(),
                  FilemanEntry.builder()
                      .status("1")
                      .file(PlanCoverageLimitations.FILE_NUMBER)
                      .ien(id)
                      .fields(planLimitationFields())
                      .build(),
                  FilemanEntry.builder()
                      .status("1")
                      .file(SubscriberDates.FILE_NUMBER)
                      .ien(id)
                      .fields(subscriberDatesFields())
                      .build(),
                  FilemanEntry.builder()
                      .status("1")
                      .file(IivResponse.FILE_NUMBER)
                      .ien(id)
                      .fields(iivFields())
                      .build(),
                  FilemanEntry.builder()
                      .status("1")
                      .file(SubscriberReferenceId.FILE_NUMBER)
                      .ien(id)
                      .fields(subscriberReferenceIdFields())
                      .build()))
          .build();
    }

    private Map<String, Values> healthCareCodeInformationFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(HealthCareCodeInformation.DIAGNOSIS_CODE, Values.of("100.81", "100.81"));
      fields.put(
          HealthCareCodeInformation.DIAGNOSIS_CODE_QUALIFIER, Values.of("ICD-9-CM", "ICD-9-CM"));
      fields.put(HealthCareCodeInformation.PRIMARY_OR_SECONDARY, Values.of("PRIMARY", "P"));
      return Map.copyOf(fields);
    }

    public List<WriteableFilemanValue> healthCareCodeInformationFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(HealthCareCodeInformation.FILE_NUMBER)
              .index(1)
              .field(HealthCareCodeInformation.DIAGNOSIS_CODE)
              .value("100.81")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthCareCodeInformation.FILE_NUMBER)
              .index(1)
              .field(HealthCareCodeInformation.DIAGNOSIS_CODE_QUALIFIER)
              .value("ICD-9-CM")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthCareCodeInformation.FILE_NUMBER)
              .index(1)
              .field(HealthCareCodeInformation.PRIMARY_OR_SECONDARY)
              .value("PRIMARY")
              .build());
    }

    public List<WriteableFilemanValue> healthcareServicesDeliveryFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.SEQUENCE)
              .value("1")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.BENEFIT_QUANTITY)
              .value("365.666")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.QUANTITY_QUALIFIER)
              .value("m2")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.SAMPLE_SELECTION_MODULUS)
              .value("5")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.UNITS_OF_MEASUREMENT)
              .value("VS")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.TIME_PERIODS)
              .value("3")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.TIME_PERIOD_QUALIFIER)
              .value("32")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.DELIVERY_FREQUENCY)
              .value("N")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field(HealthcareServicesDelivery.DELIVERY_PATTERN)
              .value("G")
              .build());
    }

    public List<WriteableFilemanValue> ienMacroPointers() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(InsuranceType.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("1,8,")
              .build(),
          WriteableFilemanValue.builder()
              .file(EligibilityBenefit.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365^1^IEN}")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthCareCodeInformation.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365^1^IEN}")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberDates.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365.02^1^IEN}")
              .build(),
          WriteableFilemanValue.builder()
              .file(ServiceTypes.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365.02^1^IEN}")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberReferenceId.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365.02^1^IEN}")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberAdditionalInfo.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365.02^1^IEN}")
              .build(),
          WriteableFilemanValue.builder()
              .file(HealthcareServicesDelivery.FILE_NUMBER)
              .index(1)
              .field("IEN")
              .value("${365.02^1^IEN}")
              .build());
    }

    private Map<String, Values> iivFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(IivResponse.MESSAGE_CONTROL_ID, Values.of("MCI-1234", "MCI-1234"));
      fields.put(IivResponse.PAYER, Values.of("8", "8"));
      fields.put(IivResponse.TRACE_NUMBER, Values.of("TN-1234", "TN-1234"));
      fields.put(IivResponse.SERVICE_DATE, Values.of("01-20-2010", "01-20-2010"));
      fields.put(IivResponse.MILITARY_INFO_STATUS_CODE, Values.of("P", "P"));
      fields.put(IivResponse.MILITARY_EMPLOYMENT_STATUS, Values.of("CC", "Closed-Captions"));
      fields.put(IivResponse.MILITARY_GOVT_AFFILIATION_CODE, Values.of("C", "ARMY"));
      fields.put(IivResponse.MILITARY_PERSONNEL_DESCRIPTION, Values.of("ARMY", "ARMY"));
      fields.put(IivResponse.MILITARY_SERVICE_RANK_CODE, Values.of("C1", "CAPTAIN"));
      fields.put(IivResponse.DATE_TIME_PERIOD, Values.of("02032004-05062007", "02032004-05062007"));
      return Map.copyOf(fields);
    }

    public List<WriteableFilemanValue> iivResponseFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MESSAGE_CONTROL_ID)
              .value("MCI-1234")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.PAYER)
              .value("`8")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.TRACE_NUMBER)
              .value("TN-1234")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.SERVICE_DATE)
              .value("01-20-2010")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_INFO_STATUS_CODE)
              .value("P")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_EMPLOYMENT_STATUS)
              .value("CC")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_GOVT_AFFILIATION_CODE)
              .value("C")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_PERSONNEL_DESCRIPTION)
              .value("ARMY")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.MILITARY_SERVICE_RANK_CODE)
              .value("C1")
              .build(),
          WriteableFilemanValue.builder()
              .file(IivResponse.FILE_NUMBER)
              .index(1)
              .field(IivResponse.DATE_TIME_PERIOD)
              .value("02032004-05062007")
              .build());
    }

    private Map<String, Values> planLimitationFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(PlanCoverageLimitations.COVERAGE_CATEGORY, Values.of("MENTAL HEALTH", "4"));
      fields.put(PlanCoverageLimitations.COVERAGE_STATUS, Values.of("CONDITIONAL COVERAGE", "2"));
      fields.put(PlanCoverageLimitations.EFFECTIVE_DATE, Values.of("JAN 12, 1992", "2920112"));
      fields.put(PlanCoverageLimitations.PLAN, Values.of("BCBS OF FL", "87"));
      return Map.copyOf(fields);
    }

    public List<WriteableFilemanValue> serviceTypesFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(ServiceTypes.FILE_NUMBER)
              .index(1)
              .field(ServiceTypes.SERVICE_TYPES)
              .value("OTHER MEDICAL")
              .build());
    }

    private Map<String, Values> subscriberAdditionalInfoFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(SubscriberAdditionalInfo.SEQUENCE, Values.of("1", "01"));
      fields.put(SubscriberAdditionalInfo.PLACE_OF_SERVICE, Values.of("12", "HOME"));
      fields.put(SubscriberAdditionalInfo.QUALIFIER, Values.of("NI", "NATURE OF INJURY CODE"));
      fields.put(
          SubscriberAdditionalInfo.NATURE_OF_INJURY_CODE,
          Values.of("GR", "NATURE OF INJURY (NCCI)"));
      fields.put(
          SubscriberAdditionalInfo.NATURE_OF_INJURY_CATEGORY,
          Values.of("NI", "NATURE OF INJURY CODE"));
      fields.put(
          SubscriberAdditionalInfo.NATURE_OF_INJURY_TEXT,
          Values.of("ARM IS BORKED", "ARM IS BORKED"));
      return Map.copyOf(fields);
    }

    public List<WriteableFilemanValue> subscriberAdditionalInfoFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(SubscriberAdditionalInfo.FILE_NUMBER)
              .index(1)
              .field(SubscriberAdditionalInfo.SEQUENCE)
              .value("1")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberAdditionalInfo.FILE_NUMBER)
              .index(1)
              .field(SubscriberAdditionalInfo.PLACE_OF_SERVICE)
              .value("12")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberAdditionalInfo.FILE_NUMBER)
              .index(1)
              .field(SubscriberAdditionalInfo.QUALIFIER)
              .value("NI")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberAdditionalInfo.FILE_NUMBER)
              .index(1)
              .field(SubscriberAdditionalInfo.NATURE_OF_INJURY_CODE)
              .value("GR")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberAdditionalInfo.FILE_NUMBER)
              .index(1)
              .field(SubscriberAdditionalInfo.NATURE_OF_INJURY_CATEGORY)
              .value("NI")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberAdditionalInfo.FILE_NUMBER)
              .index(1)
              .field(SubscriberAdditionalInfo.NATURE_OF_INJURY_TEXT)
              .value("ARM IS BORKED")
              .build());
    }

    private Map<String, Values> subscriberDatesFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(SubscriberDates.DATE, Values.of("MAY 20, 2018", "05-20-2018"));
      fields.put(SubscriberDates.DATE_QUALIFIER, Values.of("PLAN BEGIN", "346"));
      return Map.copyOf(fields);
    }

    public List<WriteableFilemanValue> subscriberDatesFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(SubscriberDates.FILE_NUMBER)
              .index(1)
              .field(SubscriberDates.DATE)
              .value("05202018")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberDates.FILE_NUMBER)
              .index(1)
              .field(SubscriberDates.DATE_QUALIFIER)
              .value("PLAN BEGIN")
              .build());
    }

    private Map<String, Values> subscriberReferenceIdFields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(SubscriberReferenceId.SEQUENCE, Values.of("1", "01"));
      fields.put(SubscriberReferenceId.REFERENCE_ID, Values.of("REF123", "REF123-"));
      fields.put(SubscriberReferenceId.REFERENCE_ID_QUALIFIER, Values.of("18", "PLAN NUMBER"));
      fields.put(SubscriberReferenceId.DESCRIPTION, Values.of("BCBS", "BCBS-"));
      return Map.copyOf(fields);
    }

    public List<WriteableFilemanValue> subscriberReferenceIdFilemanValues() {
      return List.of(
          WriteableFilemanValue.builder()
              .file(SubscriberReferenceId.FILE_NUMBER)
              .index(1)
              .field(SubscriberReferenceId.SEQUENCE)
              .value("1")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberReferenceId.FILE_NUMBER)
              .index(1)
              .field(SubscriberReferenceId.REFERENCE_ID)
              .value("REF123")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberReferenceId.FILE_NUMBER)
              .index(1)
              .field(SubscriberReferenceId.REFERENCE_ID_QUALIFIER)
              .value("18")
              .build(),
          WriteableFilemanValue.builder()
              .file(SubscriberReferenceId.FILE_NUMBER)
              .index(1)
              .field(SubscriberReferenceId.DESCRIPTION)
              .value("BCBS")
              .build());
    }
  }
}
