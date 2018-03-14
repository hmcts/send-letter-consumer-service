package uk.gov.hmcts.reform.slc.services.steps.zip;

import uk.gov.hmcts.reform.slc.model.Letter;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

public final class ZipFileNameHelper {

    public static String generateName(Letter letter) {

        return String.format(
            "%s_%s_%s_%s.zip",
            letter.type,
            letter.service,
            now().format(ofPattern("ddMMyyyyHHmmss")),
            letter.id
        );
    }

    private ZipFileNameHelper() {
        // utility class constructor
    }
}
