package uk.gov.hmcts.reform.slc.services.steps.sftpupload;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
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

@Component
public class FtpUploader {

    private static final Logger logger = LoggerFactory.getLogger(FtpUploader.class);

    @Autowired
    private AppInsights insights;

    private final String hostname;
    private final int port;
    private final String fingerprint;
    private final String username;
    private final SSHClient ssh;
    private final String publicKey;
    private final String privateKey;
    private final String targetFolder;


    // region constructor
    public FtpUploader(
        @Value("${ftp.hostname}") String hostname,
        @Value("${ftp.port}") int port,
        @Value("${ftp.fingerprint}") String fingerprint,
        @Value("${ftp.user}") String username,
        SSHClient sshClient,
        @Value("${ftp.keys.public}") String publicKey,
        @Value("@{ftp.keys.private}") String privateKey,
        @Value("@{ftp.target-folder}") String targetFolder
    ) {
        this.hostname = hostname;
        this.port = port;
        this.fingerprint = fingerprint;
        this.username = username;
        this.ssh = sshClient;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.targetFolder = targetFolder;
    }
    // endregion

    public void upload(PdfDoc pdfDoc) {
        Instant start = Instant.now();

        try {

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
                sftp.getFileTransfer().upload(
                    pdfDoc,
                    String.join("/", this.targetFolder, pdfDoc.filename)
                );
            }

            insights.trackFtpUpload(Duration.between(start, Instant.now()), true);

        } catch (IOException exc) {
            insights.trackFtpUpload(Duration.between(start, Instant.now()), false);
            insights.trackException(exc);

            throw new FtpStepException("Unable to upload PDF.", exc);

        } finally {
            try {
                ssh.disconnect();
            } catch (IOException e) {
                logger.warn("Error closing ssh connection.");
            }
        }
    }
}
