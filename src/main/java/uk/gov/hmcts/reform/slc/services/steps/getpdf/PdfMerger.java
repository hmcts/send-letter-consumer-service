package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.PDFMergeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.pdfbox.io.MemoryUsageSetting.setupMainMemoryOnly;

public final class PdfMerger {

    private PdfMerger() {
        // utility class constructor
    }

    public static byte[] mergeDocuments(List<byte[]> documents) {
        ByteArrayOutputStream docOutputStream = new ByteArrayOutputStream();

        List<InputStream> inputStreams = documents.stream()
            .map(ByteArrayInputStream::new)
            .collect(toList());

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSources(inputStreams);
        pdfMergerUtility.setDestinationStream(docOutputStream);

        try {
            pdfMergerUtility.mergeDocuments(setupMainMemoryOnly());
        } catch (IOException e) {
            throw new PDFMergeException("Exception occurred while merging PDF files", e);
        }
        return docOutputStream.toByteArray();
    }
}
