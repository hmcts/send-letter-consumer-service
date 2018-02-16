package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import uk.gov.hmcts.reform.slc.model.Letter;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public final class FileNameGenerator {

    public static String generateFor(
        Letter letter,
        String extension
    ) {
        return letter.type + "-" + letter.service + "-" + md5Hex(letter.messageId) + "." + extension;
    }

    private FileNameGenerator() {
    }
}
