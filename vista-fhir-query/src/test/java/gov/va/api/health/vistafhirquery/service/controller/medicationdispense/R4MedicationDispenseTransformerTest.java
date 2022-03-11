package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.medicationdispense.R4MedicationDispenseTransformer.acceptAll;
import static gov.va.api.health.vistafhirquery.service.controller.medicationdispense.R4MedicationDispenseTransformer.acceptOnlyWithFillDateEqualTo;
import static gov.va.api.health.vistafhirquery.service.controller.medicationdispense.R4MedicationDispenseTransformer.acceptOnlyWithFillDateInRange;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
import gov.va.api.health.vistafhirquery.service.controller.medication.MedicationSamples;
import gov.va.api.health.vistafhirquery.service.controller.medication.MedicationSamples.Vista;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Fill;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Response.Results;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("ALL")
class R4MedicationDispenseTransformerTest {
  static CodeableConcept _cc(String text, List<Coding> codings) {
    return CodeableConcept.builder().text(text).coding(codings).build();
  }

  static Product _product(String name, ProductDetail detail) {
    return Product.builder().name(name).clazz(detail).build();
  }

  static List<Coding> _productCoding(String code, String display) {
    return Coding.builder()
        .system("https://www.pbm.va.gov/nationalformulary.asp")
        .code(code)
        .display(display)
        .build()
        .asList();
  }

  static ProductDetail _productDetail(String code, String name) {
    return ProductDetail.builder().code(code).name(name).build();
  }

  static Stream<MedicationDispense> _transform(VprGetPatientData.Response.Results results) {
    return _tx(results).toFhir();
  }

  static R4MedicationDispenseTransformer _tx(Results results) {
    return _tx(results, acceptAll());
  }

  static R4MedicationDispenseTransformer _tx(Results results, Predicate<Fill> fillFilter) {
    return R4MedicationDispenseTransformer.builder()
        .site("673")
        .patientIcn("p1")
        .rpcResults(results)
        .fillFilter(fillFilter)
        .build();
  }

  static R4MedicationDispenseTransformer _tx() {
    return _tx(MedicationSamples.Vista.create().results());
  }

  static Stream<Arguments> authorizingPrescription() {
    return Stream.of(
        Arguments.of(
            MedicationSamples.Vista.create().med().id().value(),
            Reference.builder().reference("MedicationRequest/sNp1+673+M33714").build().asList()),
        Arguments.of("", null),
        Arguments.of(null, null));
  }

  static Stream<Arguments> daysSupply() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of("", null),
        Arguments.of("not a decimal", null),
        Arguments.of(
            "123.456",
            SimpleQuantity.builder()
                .system("http://unitsofmeasure.org")
                .code("d")
                .unit("day")
                .value(new BigDecimal("123.456"))
                .build()));
  }

  static Stream<Arguments> destination() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(ValueOnlyXmlAttribute.of(""), null),
        Arguments.of(ValueOnlyXmlAttribute.of("NOPE"), null),
        Arguments.of(ValueOnlyXmlAttribute.of("W"), Reference.builder().display("WINDOW").build()),
        Arguments.of(
            ValueOnlyXmlAttribute.of("C"),
            Reference.builder().display("ADMINISTERED IN CLINIC").build()));
  }

  static Stream<Arguments> dosageInstruction() {
    return Stream.of(
        Arguments.of(null, null, null),
        Arguments.of("", "", null),
        Arguments.of("SIG", null, Dosage.builder().text("SIG").build().asList()),
        Arguments.of("SIG", "", Dosage.builder().text("SIG").build().asList()),
        Arguments.of(
            "SIG", "PTI", Dosage.builder().text("SIG").patientInstruction("PTI").build().asList()),
        Arguments.of(null, "PTI", Dosage.builder().patientInstruction("PTI").build().asList()),
        Arguments.of("", "PTI", Dosage.builder().patientInstruction("PTI").build().asList()));
  }

  static Stream<Arguments> facility() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(
            CodeAndNameXmlAttribute.of("673", "TAMPA (JAH VAH)"),
            Reference.builder().display("TAMPA (JAH VAH)").build()),
        Arguments.of(CodeAndNameXmlAttribute.of("673", ""), null));
  }

  private static List<Extension> fillsRemaining(int remaining) {
    return Extension.builder()
        .url("http://hl7.org/fhir/StructureDefinition/medicationdispense-refillsRemaining")
        .valueInteger(remaining)
        .build()
        .asList();
  }

  static Stream<Arguments> fillsRemaining() {
    return Stream.of(
        Arguments.of(null, "3", 1, fillsRemaining(2)),
        Arguments.of("", "3", 1, fillsRemaining(2)),
        Arguments.of("1", "3", 1, fillsRemaining(1)),
        Arguments.of(null, null, 1, null));
  }

  static Stream<Arguments> medicationCodeableConcept() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(Product.builder().build(), null),
        Arguments.of(_product("", null), null),
        Arguments.of(_product("SHAMWOW", null), _cc("SHAMWOW", null)),
        Arguments.of(
            _product(null, _productDetail("BILLY", "MAYES")),
            _cc(null, _productCoding("BILLY", "MAYES"))),
        Arguments.of(
            _product("", _productDetail("BILLY", "")), _cc(null, _productCoding("BILLY", null))),
        Arguments.of(
            _product("SHAMWOW", _productDetail("BILLY", "MAYES")),
            _cc("SHAMWOW", _productCoding("BILLY", "MAYES"))));
  }

  static Stream<Arguments> productCoding() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(ProductDetail.builder().build(), null),
        Arguments.of(_productDetail("", ""), null),
        Arguments.of(_productDetail("BILLY", "MAYES"), _productCoding("BILLY", "MAYES")),
        Arguments.of(_productDetail("", "MAYES"), _productCoding(null, "MAYES")),
        Arguments.of(_productDetail(null, "MAYES"), _productCoding(null, "MAYES")),
        Arguments.of(_productDetail("BILLY", null), _productCoding("BILLY", null)),
        Arguments.of(_productDetail("BILLY", ""), _productCoding("BILLY", null)));
  }

  static Stream<Arguments> status() {
    return Stream.of(
        Arguments.of(Fill.builder().build(), Status.in_progress),
        Arguments.of(Fill.builder().releaseDate("3050121").build(), Status.completed));
  }

  static Stream<Arguments> toFhirIgnoresUnusableFills() {
    var ignoredBecauseNoFillDate = MedicationSamples.Vista.create().fill(null, "3050121");
    var ignoredBecauseEmpty = Fill.builder().build();
    return Stream.of(Arguments.of(ignoredBecauseEmpty), Arguments.of(ignoredBecauseNoFillDate));
  }

  @ParameterizedTest
  @MethodSource
  void authorizingPrescription(String vistaId, List<Reference> mrReference) {
    assertThat(_tx().authorizingPrescription(vistaId)).isEqualTo(mrReference);
  }

  @ParameterizedTest
  @MethodSource
  void daysSupply(String fillDaysSupply, SimpleQuantity expected) {
    var actual = _tx().daysSupply(fillDaysSupply);
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource
  void destination(ValueOnlyXmlAttribute routing, Reference expected) {
    var actual = _tx().destination(routing);
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource
  void facility(CodeAndNameXmlAttribute facility, Reference expectedReference) {
    assertThat(_tx().facility(facility)).isEqualTo(expectedReference);
  }

  @Test
  void fillDateIsRequiredToBeConsideredViable() {
    var fill = MedicationSamples.Vista.create().fill();
    assertThat(_tx().isViable(fill)).isTrue();
    assertThat(_tx().isViable(fill.fillDate(null))).isFalse();
  }

  @ParameterizedTest
  @MethodSource
  void fillsRemaining(
      String fillsRemaining, String fillsAllowed, int fillSize, List<Extension> expected) {
    assertThat(_tx().fillsRemaining(fillsRemaining, fillsAllowed, fillSize)).isEqualTo(expected);
  }

  @Test
  void predicateAcceptAll() {
    var vista = Vista.create();
    assertThat(acceptAll()).accepts(null, vista.fill());
  }

  @Test
  void predicateAcceptOnlyWithFillDateEqualTo() {
    var vista = Vista.create();
    assertThat(acceptOnlyWithFillDateEqualTo("3050121"))
        .accepts(vista.fill("3050121"), vista.fill("3050121", null));
    assertThat(acceptOnlyWithFillDateEqualTo("3050121"))
        .rejects(vista.fill("3050122"), vista.fill("3050122", "3050121"));
  }

  @Test
  void predicateAcceptOnlyWithFillDateInRange() {
    var vista = Vista.create();
    var boundaries = DateSearchBoundaries.of(new String[] {"GE2005-01-21"});
    assertThat(acceptOnlyWithFillDateInRange(boundaries))
        .accepts(vista.fill("3060121"), vista.fill("3060121", null));
    assertThat(acceptOnlyWithFillDateInRange(boundaries))
        .rejects(vista.fill("3040121"), vista.fill("3040121", "3060121"));
  }

  @ParameterizedTest
  @MethodSource
  void status(Fill fill, Status status) {
    assertThat(_tx().status(fill)).isEqualTo(status);
  }

  @Test
  void toFhirIgnoresEmptyResults() {
    assertThat(_tx(VprGetPatientData.Response.Results.builder().build()).toFhir()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource
  void toFhirIgnoresUnusableFills(Fill ignoreMe) {
    var vista = MedicationSamples.Vista.create();
    var includeFill = vista.fill("3050121");
    var includeFillWithoutReleaseDate = vista.fill("3060121", null);
    var med = vista.med("1", includeFill, ignoreMe, includeFillWithoutReleaseDate);
    var transformer = _tx(vista.results(med));
    var transformed = transformer.toFhir().toList();
    assertThat(transformed).hasSize(2);
    assertThat(transformed.get(0).id()).isEqualTo(transformer.idOf(med, includeFill).toString());
    assertThat(transformed.get(1).id())
        .isEqualTo(transformer.idOf(med, includeFillWithoutReleaseDate).toString());
  }

  @Test
  void toFhirUsesOnlyFillsMatchingFileDateFilter() {
    var vista = MedicationSamples.Vista.create();
    var includeFill = vista.fill("3050121");
    var ignoredBecauseNotOnFillDate = vista.fill("3060121");
    var med = vista.med("1", includeFill, ignoredBecauseNotOnFillDate);
    var transformer = _tx(vista.results(med), acceptOnlyWithFillDateEqualTo("3050121"));
    var transformed = transformer.toFhir().toList();
    assertThat(transformed).hasSize(1);
    assertThat(transformed.get(0).id()).isEqualTo(transformer.idOf(med, includeFill).toString());
  }
}
