package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.model.Document;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.duplex.DuplexPreparator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class PdfCreator {

    @Autowired
    private AppInsights insights;

    private final PDFServiceClient client;
    private final DuplexPreparator duplexPreparator;

    public PdfCreator(PDFServiceClient client, DuplexPreparator duplexPreparator) {
        this.client = client;
        this.duplexPreparator = duplexPreparator;
    }

    public PdfDoc create(Letter letter) {
        Asserts.notNull(letter, "letter");

        List<byte[]> docs =
            letter.documents
                .stream()
                .map(this::generatePdf)
                .map(duplexPreparator::prepare)
                .collect(toList());

        byte[] finalContent = docs.get(0); // TODO: merge into one

        return new PdfDoc(
            FileNameGenerator.generateFor(letter.type, letter.service, finalContent, "pdf"),
            finalContent
        );
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
