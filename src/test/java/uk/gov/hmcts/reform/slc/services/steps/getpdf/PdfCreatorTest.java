package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.model.Document;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.duplex.DuplexPreparator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class PdfCreatorTest {

    @Mock private PDFServiceClient client;
    @Mock private DuplexPreparator duplexPreparator;
    @Mock private AppInsights insights;

    private PdfCreator pdfCreator;

    @Before
    public void setUp() {
        pdfCreator = new PdfCreator(this.client, this.duplexPreparator);

        ReflectionTestUtils.setField(pdfCreator, "insights", insights);
    }

    @Test
    public void should_require_letter_to_not_be_null() {
        assertThatThrownBy(() -> pdfCreator.create(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("letter");
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_return_a_merged_pdf_when_letter_consists_of_multiple_documents() throws IOException {
        byte[] test1Pdf = toByteArray(getResource("test1.pdf"));
        byte[] test2Pdf = toByteArray(getResource("test2.pdf"));

        given(client.generateFromHtml("t1".getBytes(), emptyMap()))
            .willReturn(test1Pdf);
        given(client.generateFromHtml("t2".getBytes(), emptyMap()))
            .willReturn(test2Pdf);
        given(duplexPreparator.prepare(test1Pdf))
            .willReturn(test1Pdf);
        given(duplexPreparator.prepare(test2Pdf))
            .willReturn(test2Pdf);

        Letter letter = new Letter(
            asList(
                new Document("t1", emptyMap()),
                new Document("t2", emptyMap())
            ),
            "type",
            "service"
        );

        // when
        PdfDoc pdf = pdfCreator.create(letter);

        // then
        assertThat(extractPdfText(pdf.content))
            .contains("test1")
            .contains("test2");

        assertThat(pdf.filename).startsWith("type-service");

        verify(client).generateFromHtml("t1".getBytes(), emptyMap());
        verify(client).generateFromHtml("t2".getBytes(), emptyMap());

        verify(duplexPreparator, times(2)).prepare(any(byte[].class));

        verify(insights, times(2)).trackPdfGenerator(any(Duration.class), eq(true));
        verifyNoMoreInteractions(client, duplexPreparator, insights);
    }

    @Test
    public void should_throw_exception_when_unable_to_generate_pdf() {
        willThrow(PDFServiceClientException.class).given(client).generateFromHtml(any(), any());

        Letter letter = new Letter(
            singletonList(new Document("template", emptyMap())),
            "type",
            "service"
        );

        assertThatThrownBy(() -> pdfCreator.create(letter))
            .isInstanceOf(PDFServiceClientException.class);

        verify(insights).trackPdfGenerator(any(Duration.class), eq(false));
        verify(insights).trackException(any(PDFServiceClientException.class));
        verifyNoMoreInteractions(insights);
    }

    private static String extractPdfText(byte[] pdfData) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(new ByteArrayInputStream(pdfData))) {
            return new PDFTextStripper().getText(pdfDocument);
        }
    }
}
