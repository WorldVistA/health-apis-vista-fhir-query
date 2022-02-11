package gov.va.api.health.vistafhirquery.service.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalLookupV1;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalV1;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalsV1;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RpcPrincipalV1ConfigTest {

  @Test
  void loadPrincipals() {
    RpcPrincipalLookupV1 testPrincipals =
        new RpcPrincipalV1Config().loadPrincipalsV1("src/test/resources/principalsV1.json");
    assertThat(testPrincipals.findByNameAndSite("SASHIMI", "222-A"))
        .isEqualTo(
            Optional.of(
                RpcPrincipalV1.builder()
                    .applicationProxyUser("ASIAN!")
                    .accessCode("ASIAN_FOOD")
                    .verifyCode("IS_STILL_GREAT")
                    .build()));
  }

  @Test
  void loadPrincipalsNullFile() {
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> new RpcPrincipalV1Config().loadPrincipalsV1(null));
  }

  @Test
  void validate() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                new RpcPrincipalV1Config().validate(RpcPrincipalsV1.builder().build(), "whatever"));
  }
}
