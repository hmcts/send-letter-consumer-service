package uk.gov.hmcts.reform.slc.services.steps.getpdf.duplex;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

/**
 * SonarQube reports as error. Max allowed - 5 parents
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DuplexException extends UnknownErrorCodeException {
    public DuplexException(Throwable cause) {
        super(AlertLevel.P2, cause);
    }
}
