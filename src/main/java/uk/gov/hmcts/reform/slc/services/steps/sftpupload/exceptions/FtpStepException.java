package uk.gov.hmcts.reform.slc.services.steps.sftpupload.exceptions;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

public class FtpStepException extends AbstractLoggingException {
    public FtpStepException(String message, Throwable cause) {
        super(AlertLevel.P1, "0", message, cause);
    }
}
