package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.junit.Test;
import uk.gov.hmcts.reform.slc.model.Letter;

import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class FileNameGeneratorTest {

    @Test
    public void should_generate_file_name_in_expected_format() {
        // given
        Letter letter = createLetter("typeA", "cmc", "098F6BCD4621D373CADE4E832627B4F6");

        // when
        String result = FileNameGenerator.generateFor(letter, "pdf");

        // then
        assertThat(result).matches("typeA-cmc-.*\\.pdf");
    }

    @Test
    public void should_always_generate_the_same_name_for_same_input() {
        // given
        Letter letter1 = createLetter("A", "B", "098F6BCD4621D373CADE4E832627B4F6");
        Letter letter2 = createLetter("A", "B", "098F6BCD4621D373CADE4E832627B4F6");

        String result1 = FileNameGenerator.generateFor(letter1, "pdf");
        String result2 = FileNameGenerator.generateFor(letter2, "pdf");

        // then
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    public void should_generate_different_names_for_different_message_id() {
        // given
        Letter letter1 = createLetter("A", "B", "5A105E8B9D40E1329780D62EA2265D8A");
        Letter letter2 = createLetter("A", "B", "AD0234829205B9033196BA818F7A872B");

        String result1 = FileNameGenerator.generateFor(letter1, "pdf");
        String result2 = FileNameGenerator.generateFor(letter2, "pdf");

        // then
        assertThat(result1).isNotEqualTo(result2);
    }

    private Letter createLetter(String type, String service, String messageId) {
        return new Letter(
            UUID.randomUUID(),
            emptyList(),
            type,
            service,
            messageId,
            emptyMap()
        );
    }
}
