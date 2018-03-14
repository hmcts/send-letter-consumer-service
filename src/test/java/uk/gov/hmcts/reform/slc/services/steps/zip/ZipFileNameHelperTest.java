package uk.gov.hmcts.reform.slc.services.steps.zip;

import org.junit.Test;
import uk.gov.hmcts.reform.slc.model.Letter;

import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class ZipFileNameHelperTest {

    @Test
    public void should_generate_expected_file_name() {
        // given
        Letter letter = new Letter(randomUUID(), emptyList(), "type", "cmc");

        // when
        String name = ZipFileNameHelper.generateName(letter);

        // then
        assertThat(name)
            .matches(Pattern.compile("type_cmc_[0-9]{14}_" + letter.id + ".zip"));
    }
}
