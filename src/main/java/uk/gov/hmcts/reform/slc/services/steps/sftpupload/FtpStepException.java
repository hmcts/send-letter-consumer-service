package uk.gov.hmcts.reform.slc.services.steps.sftpupload;

public class FtpStepException extends RuntimeException {
    public FtpStepException(String message, Throwable cause) {
        super(message, cause);
    }
}
