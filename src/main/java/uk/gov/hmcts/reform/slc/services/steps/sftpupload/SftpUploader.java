package uk.gov.hmcts.reform.slc.services.steps.sftpupload;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;

import java.io.IOException;

@Component
public class SftpUploader {

    private static final Logger logger = LoggerFactory.getLogger(SftpUploader.class);

    private final String hostname;
    private final int port;
    private final String fingerprint;
    private final String username;
    private final String password;
    private final SSHClient ssh;

    // region constructor
    public SftpUploader(
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

            ssh.connect(hostname, port);
            ssh.authPassword(username, password);
            ssh.addHostKeyVerifier(fingerprint);

            try (SFTPClient sftp = ssh.newSFTPClient()) {
                sftp.getFileTransfer().upload(pdfDoc, pdfDoc.filename);
            }

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
}
