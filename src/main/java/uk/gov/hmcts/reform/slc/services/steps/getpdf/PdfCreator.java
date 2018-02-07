package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.apache.http.util.Asserts;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.slc.model.Letter;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class PdfCreator {

    private final PDFServiceClient client;

    public PdfCreator(PDFServiceClient client) {
        this.client = client;
    }

    public List<PdfDoc> create(Letter letter) {
        Asserts.notNull(letter, "letter");

        return letter.documents
            .stream()
            .map(d -> client.generateFromHtml(d.template.getBytes(), d.values))
            .map(content -> new PdfDoc(
                    FileNameGenerator.generateFor(letter.type, letter.service, content, "pdf"),
                    content
                )
            ).collect(toList());
    }
}
