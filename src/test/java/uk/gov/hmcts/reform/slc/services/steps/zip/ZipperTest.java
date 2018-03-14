package uk.gov.hmcts.reform.slc.services.steps.zip;

import org.junit.Test;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;

import java.util.regex.Pattern;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;


public class ZipperTest {

    @Test
    public void should_generate_expected_file_name() {
        // given
        Letter letter = new Letter(randomUUID(), emptyList(), "type", "cmc");

        // when
        String name = Zipper.generateName(letter);

        // then
        assertThat(name)
            .matches(Pattern.compile("type_cmc_[0-9]{14}_" + letter.id + ".zip"));
    }

    @Test
    public void should_zip_file() throws Exception {
        byte[] fileContent = toByteArray(getResource("single_page.pdf"));

        ZippedDoc zippedDoc = new Zipper().zip(
            "hello.zip",
            new PdfDoc("single_page.pdf", fileContent)
        );

        assertThat(zippedDoc).isNotNull();
        assertThat(zippedDoc.filename).isEqualTo("hello.zip");
        assertThat(zippedDoc.content).isNotNull();
    }
}
