package uk.gov.hmcts.reform.slc.services.steps.sftpupload;

import uk.gov.hmcts.reform.slc.model.LetterPrintStatus;

import java.util.List;

public class ParsedReport {

    public final String name;
    public final List<LetterPrintStatus> statuses;

    public ParsedReport(String name, List<LetterPrintStatus> statuses) {
        this.name = name;
        this.statuses = statuses;
    }
}
