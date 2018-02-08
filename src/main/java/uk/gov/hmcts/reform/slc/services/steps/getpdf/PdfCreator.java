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

    private Letter letter;

    public PdfCreator(PDFServiceClient client) {
        this.client = client;
    }

    public List<PdfDoc> create(Letter letter) {
        Asserts.notNull(letter, "letter");
        this.letter = letter;

        List<GeneratorResult> results = letter.documents
            .stream()
            .map(this::generatePdf)
            .peek(this::trackGeneratorResult)
            .collect(toList());

        return results
            .stream()
            .peek(this::checkForException)
            .map(this::getPdfDoc)
            .collect(toList());
    }

    private GeneratorResult generatePdf(Document document) {
        GeneratorResult result = new GeneratorResult();
        Instant start = Instant.now();

        try {
            byte[] pdf = client.generateFromHtml(document.template.getBytes(), document.values);

            result.successful(Duration.between(start, Instant.now()), pdf);
        } catch (PDFServiceClientException exception) {
            result.failed(Duration.between(start, Instant.now()), exception);
        }

        return result;
    }

    private void trackGeneratorResult(GeneratorResult result) {
        insights.trackPdfGenerator(result.timeTookGenerating, result.isSuccess);

        if (!result.isSuccess) {
            insights.trackException(result.exception);
        }
    }

    private void checkForException(GeneratorResult result) {
        if (!result.isSuccess) {
            throw result.exception;
        }
    }

    private PdfDoc getPdfDoc(GeneratorResult result) {
        return new PdfDoc(
            FileNameGenerator.generateFor(letter.type, letter.service, result.result, "pdf"),
            result.result
        );
    }

    private final class GeneratorResult {

        Duration timeTookGenerating;

        boolean isSuccess;

        byte[] result;

        PDFServiceClientException exception;

        void failed(Duration timeTookGenerating, PDFServiceClientException exception) {
            this.timeTookGenerating = timeTookGenerating;
            this.isSuccess = false;
            this.result = null;
            this.exception = exception;
        }

        void successful(Duration timeTookGenerating, byte[] result) {
            this.timeTookGenerating = timeTookGenerating;
            this.isSuccess = true;
            this.result = result;
            this.exception = null;
        }
    }
}
