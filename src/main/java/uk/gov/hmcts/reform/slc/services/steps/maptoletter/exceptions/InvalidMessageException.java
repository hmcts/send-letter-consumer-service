package uk.gov.hmcts.reform.slc.services.steps.maptoletter.exceptions;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

/**
 * SonarQube reports as error. Max allowed - 5 parents
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidMessageException extends UnknownErrorCodeException {

    public InvalidMessageException(String message, Throwable cause) {
        super(AlertLevel.P4, message, cause);
    }

    public InvalidMessageException(String message) {
        this(message, null);
    }
}
