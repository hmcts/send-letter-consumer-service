package uk.gov.hmcts.reform.slc.services.servicebus.exceptions;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

public class PdfMergeException extends AbstractLoggingException {
    public PdfMergeException(String message, Throwable cause) {
        super(AlertLevel.P2, "0", message, cause);
    }
}
