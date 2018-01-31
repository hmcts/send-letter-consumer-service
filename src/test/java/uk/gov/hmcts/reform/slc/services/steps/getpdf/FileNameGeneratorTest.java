package uk.gov.hmcts.reform.slc.services.steps.getpdf;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileNameGeneratorTest {

    @Test
    public void should_generate_file_name_in_expected_format() {
        // given
        String letterType = "typeA";
        String jurisdiction = "cmc";
        byte[] content = "hello".getBytes();
        String extension = "pdf";

        // when
        String result = FileNameGenerator.generateFor(letterType, jurisdiction, content, extension);

        // then
        assertThat(result).matches("typeA-cmc-.*\\.pdf");
    }

    @Test
    public void should_always_generate_the_same_name_for_same_input() {

        String result1 = FileNameGenerator.generateFor("A", "B", "C".getBytes(), "pdf");
        String result2 = FileNameGenerator.generateFor("A", "B", "C".getBytes(), "pdf");

        // then
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    public void should_generate_different_names_for_different_file_contents() {
        String result1 = FileNameGenerator.generateFor("A", "B", "some_content".getBytes(), "pdf");
        String result2 = FileNameGenerator.generateFor("A", "B", "completely_different_content".getBytes(), "pdf");

        // then
        assertThat(result1).isNotEqualTo(result2);
    }
}
