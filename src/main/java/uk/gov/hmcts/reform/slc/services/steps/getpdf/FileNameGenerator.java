package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

public final class FileNameGenerator {

    public static String generateFor(
        String letterType,
        String jurisdiction,
        byte[] content
    ) {
        return letterType + "-" + jurisdiction + "-" + md5Hex(content);
    }

    private FileNameGenerator() {
    }
}
