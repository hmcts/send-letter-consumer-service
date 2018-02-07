package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.slc.model.Document;
import uk.gov.hmcts.reform.slc.model.Letter;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

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
    public void should_return_list_of_pdfs() {
        given(client.generateFromHtml("t1".getBytes(), emptyMap()))
            .willReturn("hello t1".getBytes());
        given(client.generateFromHtml("t2".getBytes(), emptyMap()))
            .willReturn("hello t2".getBytes());

        Letter letter = new Letter(
            asList(
                new Document("t1", emptyMap()),
                new Document("t2", emptyMap())
            ),
            "type",
            "service"
        );

        // when
        List<PdfDoc> pdfs = pdfCreator.create(letter);

        // then
        assertThat(pdfs).hasSize(2);

        assertThat(pdfs.get(0).content).isEqualTo("hello t1".getBytes());
        assertThat(pdfs.get(0).filename).isNotEmpty();

        assertThat(pdfs.get(1).content).isEqualTo("hello t2".getBytes());
        assertThat(pdfs.get(1).filename).isNotEmpty();
    }
}
