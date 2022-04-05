package gov.va.api.health.vistafhirquery.service.controller.coverage;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.r4.api.resources.RelatedPerson;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Values;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CoverageSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    public static Coverage.Bundle asBundle(
        String baseUrl, Collection<Coverage> resources, int totalRecords, BundleLink... links) {
      return Coverage.Bundle.builder()
          .resourceType("Bundle")
          .type(AbstractBundle.BundleType.searchset)
          .total(totalRecords)
          .link(Arrays.asList(links))
          .entry(
              resources.stream()
                  .map(
                      resource ->
                          Coverage.Entry.builder()
                              .fullUrl(baseUrl + "/Coverage/" + resource.id())
                              .resource(resource)
                              .search(
                                  AbstractEntry.Search.builder()
                                      .mode(AbstractEntry.SearchMode.match)
                                      .build())
                              .build())
                  .collect(Collectors.toList()))
          .build();
    }

    public static void cleanUpContainedReferencesForComparison(Coverage coverage) {
      // Contained Resource Ids
      coverage.contained().forEach(r -> r.id(null));
      // Insurance Plan Reference
      coverage.coverageClass().forEach(c -> c.value(null));
      // Organization Payor
      coverage.payor().forEach(o -> o.reference(null));
    }

    public static BundleLink link(BundleLink.LinkRelation rel, String base, String query) {
      return BundleLink.builder().relation(rel).url(base + "?" + query).build();
    }

    private List<Address> address() {
      return Address.builder()
          .state("FLORIDA")
          .city("SHANK CITY")
          .line(List.of("SHANKSVILLE LINE 1", "SHANKSVILLE LINE 2", "SHANKSVILLE LINE 3"))
          .postalCode("322310014")
          .build()
          .asList();
    }

    private Reference beneficiary(String patient) {
      return Reference.builder()
          .reference("Patient/" + patient)
          .identifier(
              Identifier.builder()
                  .type(
                      CodeableConcept.builder()
                          .coding(
                              List.of(
                                  Coding.builder()
                                      .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                      .code("MB")
                                      .display("Member Number")
                                      .build()))
                          .build())
                  .value("13579")
                  .build())
          .build();
    }

    private Organization.Contact billingContact() {
      return Organization.Contact.builder()
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("800-SHANK-BILLING")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("http://terminology.hl7.org/CodeSystem/contactentity-type")
                              .code("BILL")
                              .display("BILL")
                              .build()))
                  .build())
          .build();
    }

    private List<Coverage.CoverageClass> classes(String station, String patient) {
      return List.of(
          Coverage.CoverageClass.builder()
              .value(
                  "InsurancePlan/"
                      + RecordCoordinates.builder()
                          .site(station)
                          .file(GroupInsurancePlan.FILE_NUMBER)
                          .ien("87")
                          .build()
                          .toString())
              .type(
                  CodeableConcept.builder()
                      .coding(
                          List.of(
                              Coding.builder()
                                  .system("http://terminology.hl7.org/CodeSystem/coverage-class")
                                  .code("group")
                                  .build()))
                      .build())
              .build());
    }

    InsurancePlan containedInsurancePlan(String id) {
      return InsurancePlan.builder()
          .id(id)
          .name("GROUP OF SHANKSVILLE")
          .extension(insurancePlanExtensions())
          .identifier(
              List.of(
                  Identifier.builder()
                      .system(InsuranceBufferDefinitions.get().groupNumber().system())
                      .value("GRP123456")
                      .build(),
                  Identifier.builder()
                      .system(
                          InsuranceBufferDefinitions.get().bankingIdentificationNumber().system())
                      .value("88888888")
                      .build(),
                  Identifier.builder()
                      .system(InsuranceBufferDefinitions.get().processorControlNumber().system())
                      .value("121212121212")
                      .build()))
          .plan(plan())
          .build();
    }

    private Organization containedOrganization(String id) {
      return Organization.builder()
          .id(id)
          .active(true)
          .name("BCBS OF SHANKSVILLE")
          .address(address())
          .contact(List.of(billingContact(), precertificationContact()))
          .telecom(telecom())
          .extension(organizationExtensions())
          .build();
    }

    private RelatedPerson containedRelatedPerson(String id) {
      return RelatedPerson.builder()
          .id(id)
          .name(HumanName.builder().text("Shankey").build().asList())
          .birthDate("1994-01-01")
          .extension(
              Extension.builder()
                  .valueCode("M")
                  .url(InsuranceBufferDefinitions.get().insuredsSex().structureDefinition())
                  .build()
                  .asList())
          .identifier(
              Identifier.builder()
                  .use(Identifier.IdentifierUse.official)
                  .type(
                      CodeableConcept.builder()
                          .coding(
                              Coding.builder()
                                  .system("http://hl7.org/fhir/v2/0203")
                                  .code("SB")
                                  .build()
                                  .asList())
                          .build())
                  .system("http://hl7.org/fhir/sid/us-ssn")
                  .value("012345678")
                  .assigner(
                      Reference.builder().display("United States Social Security Number").build())
                  .build()
                  .asList())
          .address(
              Address.builder()
                  .city("SHANKSVILLE")
                  .state("FLORIDA")
                  .line(List.of("Shank Address Line 1", "Shank Address Line 2"))
                  .postalCode("54545")
                  .country("UNITED STATES")
                  .district("CUTSORTIUM")
                  .build()
                  .asList())
          .telecom(
              ContactPoint.builder()
                  .system(ContactPoint.ContactPointSystem.phone)
                  .value("555-867-5309")
                  .build()
                  .asList())
          .build();
    }

    public Coverage coverage() {
      return coverage("666", "1,8,", "1010101010V666666");
    }

    public Coverage coverage(String station, String ien, String patient) {
      return Coverage.builder()
          .id(
              PatientTypeCoordinates.builder()
                  .icn(patient)
                  .site(station)
                  .file(InsuranceType.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .meta(Meta.builder().source(station).build())
          .extension(extensions())
          .status(Status.active)
          .subscriberId("R50797108")
          .beneficiary(beneficiary(patient))
          .dependent("67890")
          .relationship(relationship())
          .period(period())
          .payor(
              List.of(
                  Reference.builder()
                      .reference(
                          "Organization/"
                              + RecordCoordinates.builder()
                                  .site(station)
                                  .file(InsuranceCompany.FILE_NUMBER)
                                  .ien("4")
                                  .build()
                                  .toString())
                      .display("BCBS OF FL")
                      .build()))
          .coverageClass(classes(station, patient))
          .order(1)
          .build();
    }

    public Coverage coverageInsuranceBufferRead(String patient, String station, String ien) {
      return Coverage.builder()
          .id(
              PatientTypeCoordinates.builder()
                  .icn(patient)
                  .site(station)
                  .file(InsuranceVerificationProcessor.FILE_NUMBER)
                  .ien(ien)
                  .build()
                  .toString())
          .period(period())
          .meta(Meta.builder().source(station).build())
          .extension(
              Extension.builder()
                  .url(InsuranceBufferDefinitions.get().serviceDate().structureDefinition())
                  .valueDate("2033-02-01")
                  .build()
                  .asList())
          .contained(
              List.of(
                  containedInsurancePlan("1"),
                  containedOrganization("2"),
                  containedRelatedPerson("3")))
          .status(Status.draft)
          .type(
              CodeableConcept.builder()
                  .coding(
                      Coding.builder()
                          .system(InsuranceBufferDefinitions.get().inqServiceTypeCode().valueSet())
                          .code("1")
                          .build()
                          .asList())
                  .build())
          .subscriberId("R50797108")
          .beneficiary(beneficiary(patient))
          .relationship(relationship())
          .coverageClass(classes(station, patient).get(0).value("#1").asList())
          .order(1)
          .dependent("2")
          .payor(
              Reference.builder()
                  .type(Organization.class.getSimpleName())
                  .reference("#2")
                  .build()
                  .asList())
          .subscriber(
              Reference.builder().type(RelatedPerson.class.getSimpleName()).reference("#3").build())
          .build();
    }

    private List<Extension> extensions() {
      return List.of(
          Extension.builder()
              .url("http://va.gov/fhir/StructureDefinition/coverage-stopPolicyFromBilling")
              .valueBoolean(true)
              .build());
    }

    private List<Extension> insurancePlanExtensions() {
      return List.of(
          Extension.builder()
              .url(InsuranceBufferStructureDefinitions.UTILIZATION_REVIEW_REQUIRED)
              .valueBoolean(true)
              .build(),
          Extension.builder()
              .url(InsuranceBufferStructureDefinitions.PRECERTIFICATION_REQUIRED)
              .valueBoolean(true)
              .build(),
          Extension.builder()
              .url(InsuranceBufferStructureDefinitions.AMBULATORY_CARE_CERTIFICATION)
              .valueBoolean(true)
              .build(),
          Extension.builder()
              .url(InsuranceBufferStructureDefinitions.EXCLUDE_PREEXISTING_CONDITION)
              .valueBoolean(false)
              .build(),
          Extension.builder()
              .url(InsuranceBufferStructureDefinitions.BENEFITS_ASSIGNABLE)
              .valueBoolean(true)
              .build());
    }

    private List<Extension> organizationExtensions() {
      return Extension.builder()
          .url(InsuranceBufferDefinitions.get().reimburse().structureDefinition())
          .valueCodeableConcept(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .code("WILL REIMBURSE")
                              .system(
                                  InsuranceBufferDefinitions.get()
                                      .reimburse()
                                      .valueDefinition()
                                      .valueSet())
                              .build()))
                  .build())
          .build()
          .asList();
    }

    private List<Organization.Contact> organzationContacts() {
      return List.of(
          Organization.Contact.builder()
              .telecom(
                  ContactPoint.builder()
                      .value("800-SHANK-BILLING")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build()
                      .asList())
              .purpose(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .system(
                                      "http://terminology.hl7.org/CodeSystem/contactentity-type")
                                  .code("BILL")
                                  .display("BILL")
                                  .build()))
                      .build())
              .build(),
          Organization.Contact.builder()
              .telecom(
                  ContactPoint.builder()
                      .value("800-SHANK-PRECERT")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build()
                      .asList())
              .purpose(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .system("https://va.gov/fhir/CodeSystem/organization-contactType")
                                  .code("PRECERT")
                                  .display("PRECERT")
                                  .build()))
                      .build())
              .build());
    }

    private Period period() {
      return Period.builder().start("1992-01-12").end("2025-01-01").build();
    }

    private List<InsurancePlan.Plan> plan() {
      return List.of(
          InsurancePlan.Plan.builder()
              .type(
                  CodeableConcept.builder()
                      .coding(
                          List.of(
                              Coding.builder()
                                  .system(InsuranceBufferDefinitions.get().typeOfPlan().valueSet())
                                  .code("40")
                                  .display("PREFERRED PROVIDER ORGANIZATION (PPO)")
                                  .build()))
                      .text("PREFERRED PROVIDER ORGANIZATION (PPO)")
                      .build())
              .build());
    }

    private Organization.Contact precertificationContact() {
      return Organization.Contact.builder()
          .telecom(
              List.of(
                  ContactPoint.builder()
                      .value("800-SHANK-PRECERT")
                      .system(ContactPoint.ContactPointSystem.phone)
                      .build()))
          .purpose(
              CodeableConcept.builder()
                  .coding(
                      Collections.singletonList(
                          Coding.builder()
                              .system("https://va.gov/fhir/CodeSystem/organization-contactType")
                              .code("PRECERT")
                              .display("PRECERT")
                              .build()))
                  .build())
          .build();
    }

    private CodeableConcept relationship() {
      return CodeableConcept.builder()
          .coding(
              List.of(
                  Coding.builder()
                      .system("http://terminology.hl7.org/CodeSystem/subscriber-relationship")
                      .code("spouse")
                      .display("Spouse")
                      .build()))
          .build();
    }

    private List<ContactPoint> telecom() {
      return ContactPoint.builder()
          .value("800-SHANK-TELE")
          .system(ContactPoint.ContactPointSystem.phone)
          .build()
          .asList();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    public Set<WriteableFilemanValue> createApiInput() {
      return Set.of(
          pointerTo(InsuranceCompany.FILE_NUMBER, "4"),
          pointerTo(GroupInsurancePlan.FILE_NUMBER, "87"),
          insuranceValue(InsuranceType.COORDINATION_OF_BENEFITS, "PRIMARY"),
          insuranceValue(InsuranceType.INSURANCE_EXPIRATION_DATE, "01-01-2025"),
          insuranceValue(InsuranceType.PT_RELATIONSHIP_HIPAA, "SPOUSE"),
          insuranceValue(InsuranceType.PHARMACY_PERSON_CODE, "67890"),
          insuranceValue(InsuranceType.PATIENT_ID, "13579"),
          insuranceValue(InsuranceType.SUBSCRIBER_ID, "R50797108"),
          insuranceValue(InsuranceType.EFFECTIVE_DATE_OF_POLICY, "01-12-1992"),
          insuranceValue(InsuranceType.STOP_POLICY_FROM_BILLING, "YES"));
    }

    public Set<WriteableFilemanValue> createInsuranceBufferInput() {
      return Set.of(
          insuranceBufferValue(InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA, "SPOUSE"),
          insuranceBufferValue(InsuranceVerificationProcessor.PATIENT_ID, "13579"),
          insuranceBufferValue(InsuranceVerificationProcessor.GROUP_NUMBER, "GRP123456"),
          insuranceBufferValue(InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1, "1"),
          insuranceBufferValue(InsuranceVerificationProcessor.EFFECTIVE_DATE, "2920112"),
          insuranceBufferValue(InsuranceVerificationProcessor.EXPIRATION_DATE, "3250101"),
          insuranceBufferValue(InsuranceVerificationProcessor.GROUP_NAME, "GROUP OF SHANKSVILLE"),
          insuranceBufferValue(InsuranceVerificationProcessor.UTILIZATION_REVIEW_REQUIRED, "YES"),
          insuranceBufferValue(InsuranceVerificationProcessor.PRECERTIFICATION_REQUIRED, "YES"),
          insuranceBufferValue(InsuranceVerificationProcessor.AMBULATORY_CARE_CERTIFICATION, "YES"),
          insuranceBufferValue(InsuranceVerificationProcessor.EXCLUDE_PREEXISTING_CONDITION, "NO"),
          insuranceBufferValue(InsuranceVerificationProcessor.BENEFITS_ASSIGNABLE, "YES"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.BANKING_IDENTIFICATION_NUMBER, "88888888"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.PROCESSOR_CONTROL_NUMBER_PCN, "121212121212"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.TYPE_OF_PLAN, "PREFERRED PROVIDER ORGANIZATION (PPO)"),
          insuranceBufferValue(InsuranceVerificationProcessor.SUBSCRIBER_ID, "R50797108"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME, "BCBS OF SHANKSVILLE"),
          insuranceBufferValue(InsuranceVerificationProcessor.PHONE_NUMBER, "800-SHANK-TELE"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.BILLING_PHONE_NUMBER, "800-SHANK-BILLING"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.PRECERTIFICATION_PHONE_NUMBER, "800-SHANK-PRECERT"),
          insuranceBufferValue(InsuranceVerificationProcessor.REIMBURSE, "WILL REIMBURSE"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.STREET_ADDRESS_LINE_1, "SHANKSVILLE LINE 1"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.STREET_ADDRESS_LINE_2, "SHANKSVILLE LINE 2"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.STREET_ADDRESS_LINE_3, "SHANKSVILLE LINE 3"),
          insuranceBufferValue(InsuranceVerificationProcessor.CITY, "SHANK CITY"),
          insuranceBufferValue(InsuranceVerificationProcessor.STATE, "FLORIDA"),
          insuranceBufferValue(InsuranceVerificationProcessor.ZIP_CODE, "322310014"),
          insuranceBufferValue(InsuranceVerificationProcessor.SUBSCRIBER_PHONE, "555-867-5309"),
          insuranceBufferValue(InsuranceVerificationProcessor.INSUREDS_SSN, "012345678"),
          insuranceBufferValue(InsuranceVerificationProcessor.NAME_OF_INSURED, "Shankey"),
          insuranceBufferValue(InsuranceVerificationProcessor.INSUREDS_SEX, "M"),
          insuranceBufferValue(InsuranceVerificationProcessor.INSUREDS_DOB, "2940101"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_1, "Shank Address Line 1"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_2, "Shank Address Line 2"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_CITY, "SHANKSVILLE"),
          insuranceBufferValue(InsuranceVerificationProcessor.COORDINATION_OF_BENEFITS, "1"),
          insuranceBufferValue(InsuranceVerificationProcessor.PHARMACY_PERSON_CODE, "2"),
          insuranceBufferValue(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_STATE, "FLORIDA"),
          insuranceBufferValue(InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_ZIP, "54545"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_COUNTRY, "UNITED STATES"),
          insuranceBufferValue(
              InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_SUBDIVISION, "CUTSORTIUM"),
          insuranceBufferValue(InsuranceVerificationProcessor.SERVICE_DATE, "02012033"));
    }

    public LhsLighthouseRpcGatewayResponse.Results createInsuranceBufferResults(String id) {
      return createInsuranceBufferResults(id, SubscriberToBeneficiaryRelationship.SPOUSE);
    }

    public LhsLighthouseRpcGatewayResponse.Results createInsuranceBufferResults(
        String id, SubscriberToBeneficiaryRelationship relationship) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file(InsuranceVerificationProcessor.FILE_NUMBER)
                      .ien(id)
                      .index("1")
                      .status("1")
                      .fields(insuranceBufferFields(relationship))
                      .build()))
          .build();
    }

    public LhsLighthouseRpcGatewayResponse.Results createInsuranceTypeResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file(InsuranceType.FILE_NUMBER)
                      .ien(id)
                      .index("1")
                      .status("1")
                      .build()))
          .build();
    }

    private Map<String, Values> fields() {
      Map<String, Values> fields = new HashMap<>();
      fields.put(InsuranceType.INSURANCE_TYPE, Values.of("BCBS OF FL", "4"));
      fields.put(InsuranceType.GROUP_PLAN, Values.of("BCBS OF FL", "87"));
      fields.put(InsuranceType.COORDINATION_OF_BENEFITS, Values.of("PRIMARY", "1"));
      fields.put(InsuranceType.SEND_BILL_TO_EMPLOYER, Values.of("0", "0"));
      fields.put(InsuranceType.ESGHP, Values.of("3", "3"));
      fields.put(InsuranceType.INSURANCE_EXPIRATION_DATE, Values.of("JAN 01, 2025", "3250101"));
      fields.put(InsuranceType.STOP_POLICY_FROM_BILLING, Values.of("1", "1"));
      fields.put(InsuranceType.PT_RELATIONSHIP_HIPAA, Values.of("SPOUSE", "01"));
      fields.put(InsuranceType.PHARMACY_PERSON_CODE, Values.of("67890", "67890"));
      fields.put(InsuranceType.PATIENT_ID, Values.of("13579", "13579"));
      fields.put(InsuranceType.SUBSCRIBER_ID, Values.of("R50797108", "R50797108"));
      fields.put(InsuranceType.EFFECTIVE_DATE_OF_POLICY, Values.of("JAN 12, 1992", "2920112"));
      return Map.copyOf(fields);
    }

    LhsLighthouseRpcGatewayResponse.Results getsManifestResults() {
      return getsManifestResults("1,8,");
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file("2.312")
                      .ien(id)
                      .fields(fields())
                      .build()))
          .build();
    }

    private Map<String, Values> insuranceBufferFields(
        SubscriberToBeneficiaryRelationship relationship) {
      Map<String, Values> fields = new HashMap<>();
      fields.put(
          InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME,
          Values.of("BCBS OF SHANKSVILLE", "BCBS OF SHANKSVILLE"));
      fields.put(InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1, Values.of("1", "1"));
      fields.put(InsuranceVerificationProcessor.PATIENT_ID, Values.of("13579", "13579"));
      fields.put(
          InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA,
          Values.of(relationship.display(), relationship.code()));
      fields.put(InsuranceVerificationProcessor.SUBSCRIBER_ID, Values.of("R50797108", "R50797108"));
      fields.put(InsuranceVerificationProcessor.EFFECTIVE_DATE, Values.of("2920112", "2920112"));
      fields.put(InsuranceVerificationProcessor.EXPIRATION_DATE, Values.of("3250101", "3250101"));
      fields.put(
          InsuranceVerificationProcessor.PHONE_NUMBER,
          Values.of("800-SHANK-TELE", "800-SHANK-TELE"));
      fields.put(
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_1,
          Values.of("SHANKSVILLE LINE 1", "SHANKSVILLE LINE 1"));
      fields.put(
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_2,
          Values.of("SHANKSVILLE LINE 2", "SHANKSVILLE LINE 2"));
      fields.put(
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_3,
          Values.of("SHANKSVILLE LINE 3", "SHANKSVILLE LINE 3"));
      fields.put(InsuranceVerificationProcessor.CITY, Values.of("SHANK CITY", "SHANK CITY"));
      fields.put(InsuranceVerificationProcessor.ZIP_CODE, Values.of("322310014", "322310014"));
      fields.put(InsuranceVerificationProcessor.STATE, Values.of("FLORIDA", "FL"));
      fields.put(
          InsuranceVerificationProcessor.SUBSCRIBER_PHONE,
          Values.of("555-867-5309", "555-867-5309"));
      fields.put(InsuranceVerificationProcessor.INSUREDS_SSN, Values.of("012345678", "012345678"));
      fields.put(InsuranceVerificationProcessor.NAME_OF_INSURED, Values.of("Shankey", "Shankey"));
      fields.put(InsuranceVerificationProcessor.INSUREDS_DOB, Values.of("2940101", "2940101"));
      fields.put(InsuranceVerificationProcessor.INSUREDS_SEX, Values.of("Male", "M"));
      fields.put(InsuranceVerificationProcessor.COORDINATION_OF_BENEFITS, Values.of("1", "1"));
      fields.put(InsuranceVerificationProcessor.PHARMACY_PERSON_CODE, Values.of("2", "2"));
      fields.put(
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_1,
          Values.of("Shank Address Line 1", "Shank Address Line 1"));
      fields.put(
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_2,
          Values.of("Shank Address Line 2", "Shank Address Line 2"));
      fields.put(
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_SUBDIVISION,
          Values.of("CUTSORTIUM", "CUTSORTIUM"));
      fields.put(
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_CITY,
          Values.of("SHANKSVILLE", "SHANKSVILLE"));
      fields.put(
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_ZIP, Values.of("54545", "54545"));
      fields.put(
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_STATE, Values.of("FLORIDA", "FL"));
      fields.put(
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_COUNTRY,
          Values.of("UNITED STATES", "UNITED STATES"));
      fields.put(
          InsuranceVerificationProcessor.BILLING_PHONE_NUMBER,
          Values.of("800-SHANK-BILLING", "800-SHANK-BILLING"));
      fields.put(
          InsuranceVerificationProcessor.PRECERTIFICATION_PHONE_NUMBER,
          Values.of("800-SHANK-PRECERT", "800-SHANK-PRECERT"));
      fields.put(
          InsuranceVerificationProcessor.REIMBURSE, Values.of("WILL REIMBURSE", "WILL REIMBURSE"));
      fields.put(
          InsuranceVerificationProcessor.GROUP_NAME,
          Values.of("GROUP OF SHANKSVILLE", "GROUP OF SHANKSVILLE"));
      fields.put(InsuranceVerificationProcessor.UTILIZATION_REVIEW_REQUIRED, Values.of("YES", "1"));
      fields.put(InsuranceVerificationProcessor.PRECERTIFICATION_REQUIRED, Values.of("YES", "1"));
      fields.put(
          InsuranceVerificationProcessor.AMBULATORY_CARE_CERTIFICATION, Values.of("YES", "1"));
      fields.put(
          InsuranceVerificationProcessor.EXCLUDE_PREEXISTING_CONDITION, Values.of("NO", "0"));
      fields.put(InsuranceVerificationProcessor.BENEFITS_ASSIGNABLE, Values.of("YES", "1"));
      fields.put(InsuranceVerificationProcessor.GROUP_NUMBER, Values.of("GRP123456", "GRP123456"));
      fields.put(
          InsuranceVerificationProcessor.BANKING_IDENTIFICATION_NUMBER,
          Values.of("88888888", "88888888"));
      fields.put(
          InsuranceVerificationProcessor.PROCESSOR_CONTROL_NUMBER_PCN,
          Values.of("121212121212", "121212121212"));
      fields.put(
          InsuranceVerificationProcessor.TYPE_OF_PLAN,
          Values.of("PREFERRED PROVIDER ORGANIZATION (PPO)", "40"));
      fields.put(InsuranceVerificationProcessor.SERVICE_DATE, Values.of("02.01.2033", "3330201"));
      return Map.copyOf(fields);
    }

    private WriteableFilemanValue insuranceBufferValue(String field, String value) {
      return WriteableFilemanValue.builder()
          .file(InsuranceVerificationProcessor.FILE_NUMBER)
          .index(1)
          .field(field)
          .value(value)
          .build();
    }

    private WriteableFilemanValue insuranceValue(String field, String value) {
      return WriteableFilemanValue.builder()
          .file(InsuranceType.FILE_NUMBER)
          .index(1)
          .field(field)
          .value(value)
          .build();
    }

    private WriteableFilemanValue pointerTo(String file, String ien) {
      return WriteableFilemanValue.builder().file(file).index(1).field("ien").value(ien).build();
    }

    public Set<WriteableFilemanValue> updateApiInput() {
      var filemanValues = new HashSet<>(createApiInput());
      filemanValues.add(pointerTo(InsuranceType.FILE_NUMBER, "1,8,"));
      return Set.copyOf(filemanValues);
    }

    public LhsLighthouseRpcGatewayResponse.Results updateCoverageWithNotExistsId() {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file("2.312")
                      .ien("doesnt-exist")
                      .index("1")
                      .status("-1")
                      .build()))
          .errors(
              List.of(
                  LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                      .data(
                          Map.of(
                              "code",
                              "601",
                              "location",
                              "FILE^LHSIBUTL",
                              "text",
                              "The entry does not exist."))
                      .build()))
          .build();
    }
  }
}
