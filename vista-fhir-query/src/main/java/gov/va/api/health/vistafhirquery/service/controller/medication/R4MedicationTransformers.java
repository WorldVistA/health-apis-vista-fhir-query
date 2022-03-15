package gov.va.api.health.vistafhirquery.service.controller.medication;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.anyBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toBigDecimal;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Dosage;
import gov.va.api.health.r4.api.elements.Dosage.DoseAndRate;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Dose;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class R4MedicationTransformers {

  /** Create a dosage from a sig and ptInstruction. */
  public static Dosage dosageInstruction(String sig, String ptInstructions, List<Dose> doses) {

    if (allBlank(sig, ptInstructions, doses)) {
      return null;
    }

    List<DoseAndRate> doseAndRates =
        Safe.stream(doses)
            .map(R4MedicationTransformers::doseAndRate)
            .filter(Objects::nonNull)
            .toList();

    return Dosage.builder()
        .text(trimToNull(sig))
        .patientInstruction(trimToNull(ptInstructions))
        .doseAndRate(doseAndRates.isEmpty() ? null : doseAndRates)
        .build();
  }

  private static DoseAndRate doseAndRate(Dose dose) {
    String unit = trimToNull(dose.units());
    BigDecimal value = toBigDecimal(dose.dose());
    if (anyBlank(unit, value)) {
      return null;
    }
    return DoseAndRate.builder()
        .doseQuantity(SimpleQuantity.builder().unit(unit).value(value).build())
        .build();
  }

  /** Create a codeableConcept from a VPR Med Domain product. */
  public static CodeableConcept medicationCodeableConcept(Product product) {
    if (product == null || allBlank(product.name(), product.clazz())) {
      return null;
    }
    var coding = productCoding(product.clazz());
    return CodeableConcept.builder()
        .text(trimToNull(product.name()))
        .coding(coding == null ? null : coding.asList())
        .build();
  }

  /** Create a MedicationRequest id from and vista id, patientIcn and site. */
  public static String medicationRequestIdFrom(String vistaMedId, String patientIcn, String site) {
    if (isBlank(vistaMedId)) {
      return null;
    }
    return toResourceId(patientIcn, site, VprGetPatientData.Domains.meds, vistaMedId);
  }

  /** Create a coding from a VPR Med Domain productDetail. */
  public static Coding productCoding(ProductDetail maybeDetail) {
    if (isBlank(maybeDetail)) {
      return null;
    }
    if (allBlank(maybeDetail.code(), maybeDetail.name())) {
      return null;
    }
    return Coding.builder()
        .system("https://www.pbm.va.gov/nationalformulary.asp")
        .code(maybeDetail.code())
        .display(maybeDetail.name())
        .build();
  }

  /** Create a SimpleQuantity using a decimal string and unit (form). */
  public static SimpleQuantity quantity(String quantity, String form) {
    var decimalValue = toBigDecimal(quantity);
    if (decimalValue == null) {
      return null;
    }
    return SimpleQuantity.builder().value(decimalValue).unit(form).build();
  }
}
