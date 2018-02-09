package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.junit.Test;
import uk.gov.hmcts.reform.slc.services.servicebus.exceptions.PDFMergeException;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PDFMergerTest {

    @Test
    public void should_return_a_merged_pdf_when_multiple_documents_are_sent() throws IOException {
        //given
        byte[] test1PDF = toByteArray(getResource("test1.pdf"));
        byte[] test2PDF = toByteArray(getResource("test1.pdf"));
        byte[] test1Test2MergedPDF = toByteArray(getResource("test1-test2-merged.pdf"));

        //when
        byte[] mergedPDF = PDFMerger.mergeDocuments(asList(test1PDF, test2PDF));

        // then
        assertThat(mergedPDF).contains(test1Test2MergedPDF);
    }

    @Test
    public void should_return_a_merged_pdf_same_as_original_pdf_when_single_pdf_is_sent() throws IOException {
        //given
        byte[] testPDF = toByteArray(getResource("test1.pdf"));
        byte[] expectedMergedPDF = toByteArray(getResource("test1.pdf"));

        //when
        byte[] actualMergedPDF = PDFMerger.mergeDocuments(singletonList(testPDF));

        // then
        assertThat(actualMergedPDF).contains(expectedMergedPDF);
    }

    @Test
    public void should_throw_pdf_merge_exception_when_doc_is_not_pdf_stream() {
        assertThatThrownBy(() -> PDFMerger.mergeDocuments(singletonList("test".getBytes())))
            .isInstanceOf(PDFMergeException.class);
    }
}
