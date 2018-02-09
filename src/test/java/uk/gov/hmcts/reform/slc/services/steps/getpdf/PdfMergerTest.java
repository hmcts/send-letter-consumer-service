package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.PDFMergeException;

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
        byte[] test1PDF = toByteArray(getResource("test1.pdf"));
        byte[] test2PDF = toByteArray(getResource("test2.pdf"));

        //when
        byte[] mergedPDF = PdfMerger.mergeDocuments(asList(test1PDF, test2PDF));

        // then
        assertThat(extractPdfText(mergedPDF))
            .contains("test1")
            .contains("test2");
    }

    @Test
    public void should_return_a_merged_pdf_same_as_original_pdf_when_single_pdf_is_sent() throws IOException {
        //given
        byte[] testPDF = toByteArray(getResource("test1.pdf"));

        //when
        byte[] actualMergedPDF = PdfMerger.mergeDocuments(singletonList(testPDF));

        // then
        assertThat(extractPdfText(actualMergedPDF)).contains("test1");
    }

    @Test
    public void should_throw_pdf_merge_exception_when_doc_is_not_pdf_stream() {
        assertThatThrownBy(() -> PdfMerger.mergeDocuments(singletonList("test".getBytes())))
            .isInstanceOf(PDFMergeException.class);
    }

    private static String extractPdfText(byte[] pdfData) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(new ByteArrayInputStream(pdfData))) {
            return new PDFTextStripper().getText(pdfDocument);
        }
    }
}
