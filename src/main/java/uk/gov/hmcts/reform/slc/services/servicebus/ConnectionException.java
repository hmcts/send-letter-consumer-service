package uk.gov.hmcts.reform.slc.services.servicebus;

public class ConnectionException extends RuntimeException {
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
