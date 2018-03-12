package uk.gov.hmcts.reform.slc.services.steps.getpdf.duplex;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

public class DuplexException extends AbstractLoggingException {
    public DuplexException(Throwable cause) {
        super(AlertLevel.P2, "0", cause);
    }
}
