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
        UUID letterId = UUID.randomUUID();
        Letter letter = createLetter(letterId, "typeA", "cmc");

        // when
        String result = FileNameGenerator.generateFor(letter, "pdf");

        // then
        assertThat(result).isEqualTo("typeA_cmc_" + letterId + ".pdf");
    }

    @Test
    public void should_always_generate_the_same_name_for_same_letter() {
        // given
        UUID letterId = UUID.randomUUID();
        Letter letter1 = createLetter(letterId, "A", "B");
        Letter letter2 = createLetter(letterId, "A", "B");

        String result1 = FileNameGenerator.generateFor(letter1, "pdf");
        String result2 = FileNameGenerator.generateFor(letter2, "pdf");

        // then
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    public void should_generate_different_names_for_different_letters() {
        // given
        Letter letter1 = createLetter(UUID.randomUUID(), "A", "B");
        Letter letter2 = createLetter(UUID.randomUUID(), "C", "D");

        String result1 = FileNameGenerator.generateFor(letter1, "pdf");
        String result2 = FileNameGenerator.generateFor(letter2, "pdf");

        // then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    public void should_generate_different_names_for_same_letters_with_different_id() {
        // given
        Letter letter1 = createLetter(UUID.randomUUID(), "A", "B");
        Letter letter2 = createLetter(UUID.randomUUID(), "A", "B");

        String result1 = FileNameGenerator.generateFor(letter1, "pdf");
        String result2 = FileNameGenerator.generateFor(letter2, "pdf");

        // then
        assertThat(result1).isNotEqualTo(result2);
    }

    private Letter createLetter(UUID id, String type, String service) {
        return new Letter(
            id,
            emptyList(),
            type,
            service,
            "098F6BCD4621D373CADE4E832627B4F6",
            emptyMap()
        );
    }
}
