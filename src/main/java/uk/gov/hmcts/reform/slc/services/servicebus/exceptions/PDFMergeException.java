package uk.gov.hmcts.reform.slc.services.servicebus.exceptions;

public class PDFMergeException extends RuntimeException {
    private static final long serialVersionUID = 4852862728499304988L;

    public PDFMergeException(String message, Throwable cause) {
        super(message, cause);
    }
}
