package uk.gov.hmcts.reform.sendletter;

import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.lang.time.DateUtils.addMilliseconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.util.DateUtil.now;

public class ProcessMessageTest extends FunctionalTestSuite {

    @Test
    public void should_transform_message_from_the_queue_into_file_on_sftp_server() throws Exception {
        String jwt = signIn();
        String letterId = sendPrintLetterRequest(jwt, sampleLetterRequestJson(1));

        try (SFTPClient sftp = getSftpClient()) {
            RemoteResourceInfo sftpFile = waitForFileOnSftp(sftp, letterId);

            assertThat(sftpFile.getName()).matches(getZipFileNamePattern(letterId));

            try (RemoteFile zipFile = sftp.open(sftpFile.getPath())) {
                PdfFile pdfFile = unzipFile(zipFile);
                assertThat(pdfFile.name).matches(getPdfFileNamePattern(letterId));

                PDDocument pdfDocument = PDDocument.load(pdfFile.content);
                assertThat(pdfDocument.getNumberOfPages()).isEqualTo(2);
            }
        }
    }

    @Test
    public void should_merge_multiple_documents_into_one() throws Exception {
        int numberOfDocuments = 3;
        String jwt = signIn();
        String letterId = sendPrintLetterRequest(jwt, sampleLetterRequestJson(numberOfDocuments));

        try (SFTPClient sftp = getSftpClient()) {
            RemoteResourceInfo sftpFile = waitForFileOnSftp(sftp, letterId);

            try (RemoteFile zipFile = sftp.open(sftpFile.getPath())) {
                PDDocument pdfDocument = PDDocument.load(unzipFile(zipFile).content);

                // the resulting page count should be equal to number of documents * 2 pages each
                assertThat(pdfDocument.getNumberOfPages()).isEqualTo(numberOfDocuments * 2);
            }
        }
    }

    private RemoteResourceInfo waitForFileOnSftp(
        SFTPClient sftp, String letterId
    ) throws IOException, InterruptedException {
        Date waitUntil = addMilliseconds(now(), maxWaitForFtpFileInMs);

        List<RemoteResourceInfo> matchingFiles;

        while (!now().after(waitUntil)) {
            matchingFiles = sftp.ls(ftpTargetFolder, file -> file.getName().contains(letterId));

            if (matchingFiles.size() == 1) {
                return matchingFiles.get(0);
            } else if (matchingFiles.size() > 1) {
                String failMessage = String.format(
                    "Expected one file with name containing '%s'. Found %d",
                    letterId,
                    matchingFiles.size()
                );

                fail(failMessage);
            } else {
                Thread.sleep(1000);
            }
        }

        throw new AssertionError("The expected file didn't appear on SFTP server");
    }

    private PdfFile unzipFile(RemoteFile zipFile) throws IOException {
        try (ZipInputStream zipStream = getZipInputStream(zipFile)) {
            ZipEntry firstEntry = zipStream.getNextEntry();
            byte[] pdfContent = readAllBytes(zipStream);

            ZipEntry secondEntry = zipStream.getNextEntry();
            assertThat(secondEntry).as("second file in zip").isNull();

            String pdfName = firstEntry.getName();

            return new PdfFile(pdfName, pdfContent);
        }
    }

    private static class PdfFile {
        public final String name;
        public final byte[] content;

        public PdfFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }
    }
}
