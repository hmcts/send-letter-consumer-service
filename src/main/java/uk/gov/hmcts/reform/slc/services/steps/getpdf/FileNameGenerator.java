package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import uk.gov.hmcts.reform.slc.model.Letter;

public final class FileNameGenerator {

    public static String generateFor(
        Letter letter,
        String extension
    ) {
        return letter.type + "_" + letter.service + "_" + letter.id + "." + extension;
    }

    private FileNameGenerator() {
    }
}
