package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.PdfMergeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PdfMergerTest {

    @Test
    public void should_return_a_merged_pdf_when_multiple_documents_are_sent() throws IOException {
        //given
        byte[] test1Pdf = toByteArray(getResource("test1.pdf"));
        byte[] test2Pdf = toByteArray(getResource("test2.pdf"));

        //when
        byte[] mergedPdf = PdfMerger.mergeDocuments(asList(test1Pdf, test2Pdf));

        // then
        assertThat(extractPdfText(mergedPdf))
            .contains("test1")
            .contains("test2");
    }

    @Test
    public void should_return_a_merged_pdf_same_as_original_pdf_when_single_pdf_is_sent() throws IOException {
        //given
        byte[] testPdf = toByteArray(getResource("test1.pdf"));

        //when
        byte[] actualMergedPdf = PdfMerger.mergeDocuments(singletonList(testPdf));

        // then
        assertThat(extractPdfText(actualMergedPdf)).contains("test1");
    }

    @Test
    public void should_throw_pdf_merge_exception_when_doc_is_not_pdf_stream() {
        assertThatThrownBy(() -> PdfMerger.mergeDocuments(asList("test1".getBytes(), "test2".getBytes())))
            .isInstanceOf(PdfMergeException.class);
    }

    private static String extractPdfText(byte[] pdfData) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(new ByteArrayInputStream(pdfData))) {
            return new PDFTextStripper().getText(pdfDocument);
        }
    }
}
