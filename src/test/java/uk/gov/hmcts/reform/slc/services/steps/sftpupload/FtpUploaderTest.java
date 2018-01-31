package uk.gov.hmcts.reform.slc.services.steps.sftpupload;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import net.schmizz.sshj.xfer.LocalSourceFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.exceptions.FtpStepException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class FtpUploaderTest {

    @Mock private SSHClient sshClient;
    @Mock private SFTPClient sftpClient;
    @Mock private SFTPFileTransfer sftpFileTransfer;

    private FtpUploader uploader;

    @Before
    public void setUp() throws Exception {
        given(sshClient.newSFTPClient()).willReturn(sftpClient);
        given(sftpClient.getFileTransfer()).willReturn(sftpFileTransfer);

        uploader = new FtpUploader(
            "hostname",
            22,
            "d8:2f:cd:a0:ce:d4:a0:c9:93:09:be:43:4b:20:49:b3",
            "user",
            "pass",
            sshClient
        );
    }

    @Test
    public void should_not_throw_an_exception_if_closing_connection_fails() throws Exception {
        // given
        doThrow(IOException.class).when(sshClient).close();

        // when
        Throwable exc = catchThrowable(() -> uploader.upload(new PdfDoc("hello.pdf", "hello".getBytes())));

        // then
        assertThat(exc).isNull();
    }

    @Test
    public void should_thrown_an_exception_if_uploading_file_fails() throws Exception {
        // given
        doThrow(IOException.class).when(sftpFileTransfer).upload(any(LocalSourceFile.class), any());

        // when
        Throwable exc = catchThrowable(() -> uploader.upload(new PdfDoc("hello.pdf", "hello".getBytes())));

        // then
        assertThat(exc)
            .isInstanceOf(FtpStepException.class)
            .hasMessageContaining("upload");
    }
}
