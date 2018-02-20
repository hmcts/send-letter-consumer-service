package uk.gov.hmcts.reform.slc.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.FileNameHelper;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.FileNameHelper.UnableToExtractIdFromFileNameException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
public class ReportParser {

    private static final Logger logger = LoggerFactory.getLogger(ReportParser.class);

    public List<LetterPrintStatus> parse(byte[] report) {
        try (CSVParser parser = parserFor(report)) {

            return stream(parser.spliterator(), false)
                .map(row -> toPrintStatus(row))
                .filter(status -> status != null)
                .collect(toList());

        } catch (IOException exc) {
            throw new ReportParsingException(exc);
        }
    }

    private CSVParser parserFor(byte[] csv) throws IOException {
        return CSVFormat
            .DEFAULT
            .withHeader()
            .parse(new InputStreamReader(new ByteArrayInputStream(csv)));
    }

    /**
     * Converts cvs row into a letter print status object.
     * Returns null if conversion fails.
     */
    private LetterPrintStatus toPrintStatus(CSVRecord record) {
        try {
            return new LetterPrintStatus(
                FileNameHelper.extractId(record.get("Filename")),
                ZonedDateTime.parse(record.get("Date") + "T" + record.get("Time") + "Z")
            );
        } catch (UnableToExtractIdFromFileNameException exc) {
            logger.error("Error extracting id", exc);
            return null;
        } catch (DateTimeParseException exc) {
            logger.error("Error parsing datetime.", exc);
            return null;
        }
    }

    public static class ReportParsingException extends RuntimeException {
        public ReportParsingException(Throwable cause) {
            super(cause);
        }
    }
}
