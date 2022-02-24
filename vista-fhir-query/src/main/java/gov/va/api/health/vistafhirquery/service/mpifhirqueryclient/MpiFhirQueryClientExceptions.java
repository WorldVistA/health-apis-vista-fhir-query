package gov.va.api.health.vistafhirquery.service.mpifhirqueryclient;

public class MpiFhirQueryClientExceptions {
  static class MpiFhirQueryClientException extends RuntimeException {
    MpiFhirQueryClientException(String message, Throwable cause) {
      super(message, cause);
    }

    public MpiFhirQueryClientException(String message) {
      super(message);
    }
  }

  public static final class MpiFhirQueryRequestFailed extends MpiFhirQueryClientException {

    public MpiFhirQueryRequestFailed(String message) {
      super(message);
    }

    public MpiFhirQueryRequestFailed(String message, Throwable cause) {
      super(message, cause);
    }

    public static MpiFhirQueryRequestFailed because(String message) {
      return new MpiFhirQueryRequestFailed(message);
    }
  }
}
