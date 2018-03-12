package uk.gov.hmcts.reform.slc.services.servicebus.exceptions;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

public class ConnectionException extends AbstractLoggingException {
    public ConnectionException(String message, Throwable cause) {
        super(AlertLevel.P1, "0", message, cause);
    }
}
