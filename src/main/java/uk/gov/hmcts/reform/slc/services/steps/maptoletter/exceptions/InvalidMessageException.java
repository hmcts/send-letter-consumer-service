package uk.gov.hmcts.reform.slc.services.steps.maptoletter.exceptions;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

import static uk.gov.hmcts.reform.slc.logging.ErrorCode.UNKNOWN;

public class InvalidMessageException extends AbstractLoggingException {

    public InvalidMessageException(String message, Throwable cause) {
        super(AlertLevel.P4, UNKNOWN, message, cause);
    }

    public InvalidMessageException(String message) {
        this(message, null);
    }
}
