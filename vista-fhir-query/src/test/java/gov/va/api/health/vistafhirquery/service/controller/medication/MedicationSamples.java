package gov.va.api.health.vistafhirquery.service.controller.medication;

import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.FilemanDate;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Dose;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Fill;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Product.ProductDetail;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Provider;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MedicationSamples {

  @NoArgsConstructor(staticName = "create")
  public static class Vista {

    public Fill fill() {
      return fill("3110507", "3110508");
    }

    public Fill fill(String fillDate) {
      var oneDayLater =
          FilemanDate.from(fillDate, ZoneId.of("UTC")).instant().plus(1, ChronoUnit.DAYS);
      String releaseDate = FilemanDate.from(oneDayLater).formatAsDateTime(ZoneId.of("UTC"));
      return fill(fillDate, releaseDate);
    }

    public Fill fill(String fillDate, String releaseDate) {
      return Fill.builder()
          .fillDate(fillDate)
          .fillRouting("W")
          .fillQuantity("30")
          .fillDaysSupply("8")
          .releaseDate(releaseDate)
          .build();
    }

    public Med med(String id) {
      return med(id, fill());
    }

    public Med med(String id, Fill... fills) {
      return Med.builder()
          .id(ValueOnlyXmlAttribute.of(id))
          .facility(CodeAndNameXmlAttribute.of("673", "TAMPA (JAH VAH)"))
          .routing(ValueOnlyXmlAttribute.of("W"))
          .vaStatus(ValueOnlyXmlAttribute.of("DISCONTINUED"))
          .orderingProvider(Provider.builder().name("HEMANN, LYNDA").build())
          .ordered(ValueOnlyXmlAttribute.of("3110301.113217"))
          .expires(ValueOnlyXmlAttribute.of("3120301"))
          .fillsAllowed(ValueOnlyXmlAttribute.of("3"))
          .form(ValueOnlyXmlAttribute.of("TAB"))
          .quantity(ValueOnlyXmlAttribute.of("30"))
          .vaType(ValueOnlyXmlAttribute.of("I"))
          .sig("TAKE 1 TAB BY MOUTH EVERY DAY")
          .ptInstructions(ValueOnlyXmlAttribute.of("take with food"))
          .dose(List.of(Dose.builder().dose("1.0").units("mg").build()))
          .fill(fills == null ? null : Arrays.asList(fills))
          .fillsRemaining(ValueOnlyXmlAttribute.of("3"))
          .product(
              List.of(
                  Product.builder()
                      .name("WARFARIN")
                      .clazz(ProductDetail.builder().name("ANTICOAGULANTS").code("BL110").build())
                      .build()))
          .build();
    }

    public Meds.Med med() {
      return med("33714");
    }

    public VprGetPatientData.Response.Results results() {
      return results(med());
    }

    public VprGetPatientData.Response.Results results(Med med) {
      return VprGetPatientData.Response.Results.builder()
          .version("1.13")
          .timeZone("-0500")
          .meds(Meds.builder().medResults(List.of(med)).build())
          .build();
    }
  }
}
