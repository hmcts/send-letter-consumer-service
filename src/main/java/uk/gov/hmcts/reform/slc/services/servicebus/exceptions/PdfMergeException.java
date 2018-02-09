package uk.gov.hmcts.reform.slc.services.servicebus.exceptions;

public class PdfMergeException extends RuntimeException {
    private static final long serialVersionUID = 4852862728499304988L;

    public PdfMergeException(String message, Throwable cause) {
        super(message, cause);
    }
}
