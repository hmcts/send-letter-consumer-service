package uk.gov.hmcts.reform.slc.services.steps.getpdf;

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

import java.time.Duration;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
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

    @Mock
    private PDFServiceClient client;

    @Mock
    private AppInsights insights;

    private PdfCreator pdfCreator;

    private final Letter letter = new Letter(
        asList(
            new Document("t1", emptyMap()),
            new Document("t2", emptyMap())
        ),
        "type",
        "service"
    );

    @Before
    public void setUp() {
        pdfCreator = new PdfCreator(this.client);

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
    public void should_return_list_of_pdfs() {
        given(client.generateFromHtml("t1".getBytes(), emptyMap()))
            .willReturn("hello t1".getBytes());
        given(client.generateFromHtml("t2".getBytes(), emptyMap()))
            .willReturn("hello t2".getBytes());

        // when
        List<PdfDoc> pdfs = pdfCreator.create(letter);

        // then
        assertThat(pdfs).hasSize(2);

        assertThat(pdfs.get(0).content).isEqualTo("hello t1".getBytes());
        assertThat(pdfs.get(0).filename).isNotEmpty();

        assertThat(pdfs.get(1).content).isEqualTo("hello t2".getBytes());
        assertThat(pdfs.get(1).filename).isNotEmpty();

        verify(insights, times(2)).trackPdfGenerator(any(Duration.class), eq(true));
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_throw_exception_when_unable_to_generate_one_pdf() {
        willThrow(PDFServiceClientException.class)
            .willReturn("hello template".getBytes())
            .given(client).generateFromHtml(any(), any());

        assertThatThrownBy(() -> pdfCreator.create(letter))
            .isInstanceOf(PDFServiceClientException.class);

        verify(insights).trackPdfGenerator(any(Duration.class), eq(true));
        verify(insights).trackPdfGenerator(any(Duration.class), eq(false));
        verify(insights).trackException(any(PDFServiceClientException.class));
        verifyNoMoreInteractions(insights);
    }

    @Test
    public void should_throw_exception_when_unable_to_generate_all_pdfs() {
        willThrow(PDFServiceClientException.class).given(client).generateFromHtml(any(), any());

        assertThatThrownBy(() -> pdfCreator.create(letter))
            .isInstanceOf(PDFServiceClientException.class);

        verify(insights, times(2)).trackPdfGenerator(any(Duration.class), eq(false));
        verify(insights, times(2)).trackException(any(PDFServiceClientException.class));
        verifyNoMoreInteractions(insights);
    }
}
