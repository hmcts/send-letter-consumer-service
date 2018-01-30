package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.slc.model.Letter;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class PdfCreatorTest {

    @Mock PDFServiceClient client;

    private PdfCreator pdfCreator;

    @Before
    public void setUp() throws Exception {
        this.pdfCreator = new PdfCreator(this.client);
    }

    @Test
    public void should_require_letter_to_not_be_null() {
        assertThatThrownBy(() -> pdfCreator.create(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("letter");
    }

    @Test
    public void should_return_pdf_object() {
        given(client.generateFromHtml(any(), any()))
            .willReturn("hello".getBytes());

        PdfDoc pdfDoc = pdfCreator.create(new Letter("template", emptyMap(), "type", "service"));

        assertThat(pdfDoc).isNotNull();
        assertThat(pdfDoc.content).isEqualTo("hello".getBytes());
        assertThat(pdfDoc.filename).isNotEmpty();
    }
}
