package uk.gov.hmcts.reform.slc.services.steps.getpdf.duplex;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

public class DuplexException extends UnknownErrorCodeException {
    public DuplexException(Throwable cause) {
        super(AlertLevel.P2, cause);
    }
}
