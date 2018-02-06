package uk.gov.hmcts.reform.slc.services.steps.sftpupload;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.exceptions.FtpStepException;

import java.io.IOException;

@Component
public class FtpUploader {

    private static final Logger logger = LoggerFactory.getLogger(FtpUploader.class);

    private final String hostname;
    private final int port;
    private final String fingerprint;
    private final String username;
    private final String password;
    private final SSHClient ssh;

    // region constructor
    public FtpUploader(
        @Value("${sftp.hostname}") String hostname,
        @Value("${sftp.port}") int port,
        @Value("${sftp.fingerprint}") String fingerprint,
        @Value("${sftp.username}") String username,
        @Value("${sftp.password}") String password,
        SSHClient sshClient
    ) {
        this.hostname = hostname;
        this.port = port;
        this.fingerprint = fingerprint;
        this.username = username;
        this.password = password;
        this.ssh = sshClient;
    }
    // endregion

    public void upload(PdfDoc pdfDoc) {
        try {
            SFTPFileTransfer fileTransfer = getSftpFileTransfer();

            fileTransfer.upload(pdfDoc, pdfDoc.filename);

        } catch (IOException exc) {
            throw new FtpStepException("Unable to upload PDF.", exc);

        } finally {
            try {
                ssh.disconnect();
            } catch (IOException e) {
                logger.warn("Error closing ssh connection.");
            }
        }
    }

    private SFTPFileTransfer getSftpFileTransfer() {
        try {
            ssh.addHostKeyVerifier(fingerprint);
            ssh.connect(hostname, port);
            ssh.authPassword(username, password);

            try (SFTPClient sftp = ssh.newSFTPClient()) {
                return sftp.getFileTransfer();
            }
        } catch (IOException exc) {
            throw new FtpStepException("Unable to connect to sftp", exc);
        }
    }
}
