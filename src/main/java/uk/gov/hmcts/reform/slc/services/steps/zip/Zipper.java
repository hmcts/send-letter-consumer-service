package uk.gov.hmcts.reform.slc.services.steps.zip;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.model.Letter;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

@Component
public class Zipper {

    public byte[] zipBytes(String filename, byte[] input) throws IOException {

        ZipEntry entry = new ZipEntry(filename);
        entry.setSize(input.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.putNextEntry(entry);
        zos.write(input);
        zos.closeEntry();
        zos.close();

        return baos.toByteArray();
    }

    public ZippedDoc zip(String zipFileName, PdfDoc pdfDoc) throws IOException {
        byte[] zipContent = zipBytes(pdfDoc.filename, pdfDoc.content);

        return new ZippedDoc(
            zipFileName,
            zipContent
        );
    }

    public static String generateName(Letter letter) {

        return String.format(
            "%s_%s_%s_%s.zip",
            letter.type,
            letter.service,
            now().format(ofPattern("yyyyMMddHHmmss")),
            letter.id
        );
    }
}
