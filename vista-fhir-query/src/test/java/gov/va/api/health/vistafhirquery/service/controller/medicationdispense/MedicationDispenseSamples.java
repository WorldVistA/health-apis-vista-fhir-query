package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.SimpleQuantity;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Fill;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.math.BigDecimal;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MedicationDispenseSamples {

  @NoArgsConstructor(staticName = "create")
  public static class R4 {

    public MedicationDispense medicationDispense() {
      return medicationDispense("sNp1+673+MYOUR_VISTA_ID_HERE");
    }

    public MedicationDispense medicationDispense(String id) {
      return MedicationDispense.builder()
          .id(id)
          .meta(Meta.builder().source("673").build())
          .identifier(List.of(Identifier.builder().value("98765A").build()))
          .quantity(SimpleQuantity.builder().value(BigDecimal.valueOf(30)).unit("TAB").build())
          .whenPrepared("2004-08-01T00:00:00Z")
          .whenHandedOver("2004-08-02T00:00:00Z")
          .status(Status.in_progress)
          .medicationCodeableConcept(CodeableConcept.builder()
              .text("IBUPROFEN")
              .coding(Coding.builder()
                  .system("https://www.pbm.va.gov/nationalformulary.asp")
                  .code("MS102")
                  .display("NONSALICYLATE NSAIs,ANTIRHEUMATIC")
                  .build()
                  .asList())
              .build())
          .daysSupply(SimpleQuantity.builder().value(BigDecimal.valueOf(30)).build())
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class Vista {

    public Meds.Med med() {
      return med("YOUR_VISTA_ID_HERE");
    }

    public Meds.Med med(String id) {
      return Med.builder()
          .id(ValueOnlyXmlAttribute.of(id))
          .facility(CodeAndNameXmlAttribute.of("673", "TAMPA (JAH VAH)"))
          .prescription(ValueOnlyXmlAttribute.of("98765A"))
          .quantity(ValueOnlyXmlAttribute.of("30"))
          .form(ValueOnlyXmlAttribute.of("TAB"))
          .fill(List.of(Fill.builder().fillDate("3040801").releaseDate("3040802").build()))
          .status(ValueOnlyXmlAttribute.of("HOLD"))
          .product(List.of(Product.builder()
              .name("IBUPROFEN")
              .clazz(ProductDetail.builder()
                  .code("MS102")
                  .name("NONSALICYLATE NSAIs,ANTIRHEUMATIC")
                  .build())
              .build()))
          .daysSupply(ValueOnlyXmlAttribute.of("30"))
          .build();
    }

    public List<Meds.Med> meds() {
      return List.of(med());
    }

    public VprGetPatientData.Response.Results results() {
      return results(med());
    }

    public VprGetPatientData.Response.Results results(Meds.Med med) {
      return VprGetPatientData.Response.Results.builder()
          .version("1.13")
          .timeZone("-0500")
          .meds(Meds.builder().medResults(List.of(med)).build())
          .build();
    }
  }
}
