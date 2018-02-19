package uk.gov.hmcts.reform.slc.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
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
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
public class ReportParser {

    private static final Logger logger = LoggerFactory.getLogger(ReportParser.class);

    public List<LetterPrintStatus> parse(byte[] report) {
        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(new InputStreamReader(new ByteArrayInputStream(report)))) {
            return stream(parser.spliterator(), false)
                .map(row -> {
                    try {
                        return new LetterPrintStatus(
                            FileNameHelper.extractId(row.get("Filename")),
                            ZonedDateTime.parse(row.get("Date") + "T" + row.get("Time") + "Z")
                        );
                    } catch (UnableToExtractIdFromFileNameException exc) {
                        logger.error("Error extracting id", exc);
                        return null;
                    } catch (DateTimeParseException exc) {
                        logger.error("Error parsing datetime.", exc);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());

        } catch (IOException exc) {
            throw new ReportParsingException(exc);
        }
    }

    public static class ReportParsingException extends RuntimeException {
        public ReportParsingException(Throwable cause) {
            super(cause);
        }
    }
}
