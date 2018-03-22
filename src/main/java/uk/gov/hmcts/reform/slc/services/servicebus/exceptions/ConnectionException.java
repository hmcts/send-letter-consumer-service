package uk.gov.hmcts.reform.slc.services.servicebus.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

public class ConnectionException extends UnknownErrorCodeException {
    public ConnectionException(String message, Throwable cause) {
        super(AlertLevel.P1, message, cause);
    }
}
