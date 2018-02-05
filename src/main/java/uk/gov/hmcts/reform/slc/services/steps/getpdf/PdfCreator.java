package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.model.Document;
import uk.gov.hmcts.reform.slc.model.Letter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class PdfCreator {

    @Autowired
    private AppInsights insights;

    private final PDFServiceClient client;

    public PdfCreator(PDFServiceClient client) {
        this.client = client;
    }

    public List<PdfDoc> create(Letter letter) {
        Asserts.notNull(letter, "letter");

        return letter.documents
            .stream()
            .map(this::generatePdf)
            .map(content -> new PdfDoc(
                    FileNameGenerator.generateFor(letter.type, letter.service, content, "pdf"),
                    content
                )
            ).collect(toList());
    }

    private byte[] generatePdf(Document document) {
        Instant start = Instant.now();

        try {
            byte[] pdf = client.generateFromHtml(document.template.getBytes(), document.values);

            insights.trackPdfGenerator(Duration.between(start, Instant.now()), true);

            return pdf;
        } catch (PDFServiceClientException exception) {
            insights.trackPdfGenerator(Duration.between(start, Instant.now()), false);
            insights.trackException(exception);

            throw exception;
        }
    }
}
