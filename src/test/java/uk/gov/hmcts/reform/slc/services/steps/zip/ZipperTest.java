package uk.gov.hmcts.reform.slc.services.steps.zip;

import org.junit.Test;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipInputStream;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;


public class ZipperTest {

    @Test
    public void should_zip_file() throws Exception {
        byte[] fileContent = toByteArray(getResource("hello.pdf"));
        byte[] expectedZipFileContent = toByteArray(getResource("hello.zip"));

        ZippedDoc result = new Zipper().zip(
            "hello.zip",
            new PdfDoc("hello.pdf", fileContent)
        );

        assertThat(result).isNotNull();
        assertThat(result.filename).isEqualTo("hello.zip");
        assertThat(asZip(result.content)).hasSameContentAs(asZip(expectedZipFileContent));

    }

    private ZipInputStream asZip(byte[] bytes) {
        return new ZipInputStream(new ByteArrayInputStream(bytes));
    }
}
