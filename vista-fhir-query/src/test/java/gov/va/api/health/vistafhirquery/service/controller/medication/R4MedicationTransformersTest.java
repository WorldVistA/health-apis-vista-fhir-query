package gov.va.api.health.vistafhirquery.service.controller.medication;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@SuppressWarnings("ALL")
class R4MedicationTransformersTest {
  private final R4MedicationTransformers _tx =
      Mockito.mock(R4MedicationTransformers.class, Mockito.CALLS_REAL_METHODS);

  static CodeableConcept _cc(String text, List<Coding> codings) {
    return CodeableConcept.builder().text(text).coding(codings).build();
  }

  static Product _product(String name, ProductDetail detail) {
    return Product.builder().name(name).clazz(detail).build();
  }

  static Coding _productCoding(String code, String display) {
    return Coding.builder()
        .system("https://www.pbm.va.gov/nationalformulary.asp")
        .code(code)
        .display(display)
        .build();
  }

  static ProductDetail _productDetail(String code, String name) {
    return ProductDetail.builder().code(code).name(name).build();
  }

  static Stream<Arguments> dosageInstruction() {
    return Stream.of(
        Arguments.of(null, null, null),
        Arguments.of("", "", null),
        Arguments.of("SIG", null, Dosage.builder().text("SIG").build()),
        Arguments.of("SIG", "", Dosage.builder().text("SIG").build()),
        Arguments.of("SIG", "PTI", Dosage.builder().text("SIG").patientInstruction("PTI").build()),
        Arguments.of(null, "PTI", Dosage.builder().patientInstruction("PTI").build()),
        Arguments.of("", "PTI", Dosage.builder().patientInstruction("PTI").build()));
  }

  static Stream<Arguments> medicationCodeableConcept() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(Product.builder().build(), null),
        Arguments.of(_product("", null), null),
        Arguments.of(_product("SHAMWOW", null), _cc("SHAMWOW", null)),
        Arguments.of(
            _product(null, _productDetail("BILLY", "MAYES")),
            _cc(null, _productCoding("BILLY", "MAYES").asList())),
        Arguments.of(
            _product("", _productDetail("BILLY", "")),
            _cc(null, _productCoding("BILLY", null).asList())),
        Arguments.of(
            _product("SHAMWOW", _productDetail("BILLY", "MAYES")),
            _cc("SHAMWOW", _productCoding("BILLY", "MAYES").asList())));
  }

  static Stream<Arguments> medicationRequestIdFrom() {
    return Stream.of(
        Arguments.of("1234", "100053443", "673", "sN100053443+673+M1234"),
        Arguments.of("", "111", "673", null),
        Arguments.of(null, "111", "673", null));
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

  @ParameterizedTest
  @MethodSource
  void dosageInstruction(String sig, String ptInstructions, Dosage expected) {
    var actual = _tx.dosageInstruction(sig, ptInstructions);
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource
  void medicationCodeableConcept(Product product, CodeableConcept expected) {
    var actual = _tx.medicationCodeableConcept(product);
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource
  void medicationRequestIdFrom(String vistaId, String patientIcn, String site, String expected) {
    assertThat(_tx.medicationRequestIdFrom(vistaId, patientIcn, site)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource
  void productCoding(ProductDetail detail, Coding expected) {
    var actual = _tx.productCoding(detail);
    assertThat(actual).isEqualTo(expected);
  }
}
