package uk.gov.hmcts.reform.slc.services.servicebus.exceptions;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

import static uk.gov.hmcts.reform.slc.logging.ErrorCode.UNKNOWN;

public class PdfMergeException extends AbstractLoggingException {
    public PdfMergeException(String message, Throwable cause) {
        super(AlertLevel.P2, UNKNOWN, message, cause);
    }
}
