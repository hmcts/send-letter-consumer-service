package uk.gov.hmcts.reform.slc.services.steps.getpdf;

// subject to change, we'll see what FTP client needs...
public class PdfDoc {

    public final String filename;
    public final byte[] content;

    public PdfDoc(String filename, byte[] content) {
        this.filename = filename;
        this.content = content;
    }
}
