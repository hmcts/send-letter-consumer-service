package uk.gov.hmcts.reform.slc.services.steps.sftpupload;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.exceptions.FtpStepException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Component
public class FtpClient {

    private static final Logger logger = LoggerFactory.getLogger(FtpClient.class);

    @Autowired
    private AppInsights insights;

    private final String hostname;
    private final int port;
    private final String fingerprint;
    private final String username;
    private final Supplier<SSHClient> sshClientSupplier;
    private final String publicKey;
    private final String privateKey;
    private final String targetFolder;
    private final String reportsFolder;


    // region constructor
    public FtpClient(
        @Value("${ftp.hostname}") String hostname,
        @Value("${ftp.port}") int port,
        @Value("${ftp.fingerprint}") String fingerprint,
        @Value("${ftp.user}") String username,
        Supplier<SSHClient> sshClientSupplier,
        @Value("${ftp.keys.public}") String publicKey,
        @Value("${ftp.keys.private}") String privateKey,
        @Value("${ftp.target-folder}") String targetFolder,
        @Value("${ftp.reports-folder}") String reportsFolder
    ) {
        this.hostname = hostname;
        this.port = port;
        this.fingerprint = fingerprint;
        this.username = username;
        this.sshClientSupplier = sshClientSupplier;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.targetFolder = targetFolder;
        this.reportsFolder = reportsFolder;
    }
    // endregion

    public void upload(PdfDoc pdfDoc) {
        Instant start = Instant.now();

        runWith(sftp -> {
            try {
                String path = String.join("/", this.targetFolder, pdfDoc.filename);
                sftp.getFileTransfer().upload(pdfDoc, path);
                insights.trackFtpUpload(Duration.between(start, Instant.now()), true);

                return null;

            } catch (IOException exc) {
                insights.trackFtpUpload(Duration.between(start, Instant.now()), false);
                insights.trackException(exc);

                throw new FtpStepException("Unable to upload PDF.", exc);
            }
        });
    }

    /**
     * Downloads ALL files from reports directory.
     */
    public List<byte[]> downloadReports() {
        return runWith(sftp -> {
            try {
                SFTPFileTransfer transfer = sftp.getFileTransfer();

                return sftp.ls(reportsFolder)
                    .stream()
                    .filter(RemoteResourceInfo::isRegularFile)
                    .map(file -> {
                        InMemoryDownloadedFile inMemoryFile = new InMemoryDownloadedFile();
                        try {
                            transfer.download(file.getPath(), inMemoryFile);
                            return inMemoryFile.getBytes();
                        } catch (IOException exc) {
                            throw new FtpStepException("Unable to download file " + file.getName(), exc);
                        }
                    })
                    .collect(toList());

            } catch (IOException exc) {
                throw new FtpStepException("Error while downloading reports", exc);
            }
        });
    }

    public void testConnection() {
        runWith(sftpClient -> null);
    }

    private <T> T runWith(Function<SFTPClient, T> action) {
        SSHClient ssh = null;

        try {
            ssh = sshClientSupplier.get();

            ssh.addHostKeyVerifier(fingerprint);
            ssh.connect(hostname, port);

            ssh.authPublickey(
                username,
                ssh.loadKeys(
                    privateKey,
                    publicKey,
                    null
                )
            );

            try (SFTPClient sftp = ssh.newSFTPClient()) {
                return action.apply(sftp);
            }
        } catch (IOException exc) {
            insights.trackException(exc);

            throw new FtpStepException("Unable to upload PDF.", exc);
        } finally {
            try {
                if (ssh != null) {
                    ssh.disconnect();
                }
            } catch (IOException e) {
                logger.warn("Error closing ssh connection.");
            }
        }
    }
}
