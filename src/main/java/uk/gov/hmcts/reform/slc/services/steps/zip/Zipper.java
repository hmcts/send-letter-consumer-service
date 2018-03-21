package uk.gov.hmcts.reform.slc.services.steps.zip;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class Zipper {

    public byte[] zipBytes(String filename, byte[] input) throws IOException {

        ZipEntry entry = new ZipEntry(filename);
        entry.setSize(input.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(entry);
            zos.write(input);
            zos.closeEntry();
        }

        return baos.toByteArray();
    }

    public ZippedDoc zip(String zipFileName, PdfDoc pdfDoc) throws IOException {
        byte[] zipContent = zipBytes(pdfDoc.filename, pdfDoc.content);

        return new ZippedDoc(
            zipFileName,
            zipContent
        );
    }
}
