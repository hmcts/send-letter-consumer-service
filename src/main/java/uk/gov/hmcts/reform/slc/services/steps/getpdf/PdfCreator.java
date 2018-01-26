package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.apache.http.util.Asserts;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.slc.model.Letter;

@Service
public class PdfCreator {

    private final PDFServiceClient client;

    public PdfCreator(PDFServiceClient client) {
        this.client = client;
    }

    public PdfDoc create(Letter letter) {

        Asserts.notNull(letter, "letter");

        byte[] content = client.generateFromHtml(
            letter.template.getBytes(),
            letter.values
        );

        // TODO: retrieve name of the service that requested sending a letter and use it here
        return new PdfDoc(
            FileNameGenerator.generateFor(letter.type, "TODO", content),
            content
        );
    }
}
