package uk.gov.hmcts.reform.slc.services.steps.getpdf.duplex;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

import static uk.gov.hmcts.reform.slc.logging.ErrorCode.UNKNOWN;

public class DuplexException extends AbstractLoggingException {
    public DuplexException(Throwable cause) {
        super(AlertLevel.P2, UNKNOWN, cause);
    }
}
