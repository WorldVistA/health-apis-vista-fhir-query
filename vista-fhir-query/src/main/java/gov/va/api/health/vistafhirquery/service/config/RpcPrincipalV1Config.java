package gov.va.api.health.vistafhirquery.service.config;

import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalLookupV1;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalsV1;
import java.io.File;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
public class RpcPrincipalV1Config {

  @Bean
  @SneakyThrows
  RpcPrincipalLookupV1 loadPrincipalsV1(
      @NonNull @Value("${vista-fhir-query.rpc-principals.file}") String principalsFile) {
    RpcPrincipalsV1 rpcPrincipals =
        new ObjectMapper().readValue(new File(principalsFile), RpcPrincipalsV1.class);
    validate(rpcPrincipals, principalsFile);
    return RpcPrincipalLookupV1.of(rpcPrincipals);
  }

  void validate(RpcPrincipalsV1 rpcPrincipals, String principalsFile) {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    var violations = validator.validate(rpcPrincipals);
    if (!violations.isEmpty()) {
      throw new IllegalArgumentException(
          "Failed to validate "
              + principalsFile
              + ": \n"
              + violations.stream().map(ConstraintViolation::getMessage).collect(joining("\n")));
    }
  }
}
