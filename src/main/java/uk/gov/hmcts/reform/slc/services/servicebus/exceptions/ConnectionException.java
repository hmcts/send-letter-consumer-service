package uk.gov.hmcts.reform.slc.services.servicebus.exceptions;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

import static uk.gov.hmcts.reform.slc.logging.ErrorCode.UNKNOWN;

public class ConnectionException extends AbstractLoggingException {
    public ConnectionException(String message, Throwable cause) {
        super(AlertLevel.P1, UNKNOWN, message, cause);
    }
}
