package uk.gov.hmcts.reform.slc.services.steps.sftpupload;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import net.schmizz.sshj.xfer.LocalSourceFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.services.steps.getpdf.PdfDoc;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.exceptions.FtpStepException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FtpClientTest {

    @Mock private SSHClient sshClient;
    @Mock private SFTPClient sftpClient;
    @Mock private SFTPFileTransfer sftpFileTransfer;
    @Mock private AppInsights insights;

    private FtpClient client;

    @Before
    public void setUp() throws Exception {
        given(sshClient.newSFTPClient()).willReturn(sftpClient);
        given(sftpClient.getFileTransfer()).willReturn(sftpFileTransfer);

        client = new FtpClient(
            "hostname",
            22,
            "d8:2f:cd:a0:ce:d4:a0:c9:93:09:be:43:4b:20:49:b3",
            "user",
            () -> sshClient,
            null,
            null,
            "target_folder",
            "reports_folder"
        );

        ReflectionTestUtils.setField(client, "insights", insights);
    }

    @Test
    public void should_not_throw_an_exception_if_closing_connection_fails() throws Exception {
        // given
        doThrow(IOException.class).when(sshClient).close();

        // when
        Throwable exc = catchThrowable(() -> client.upload(new PdfDoc("hello.pdf", "hello".getBytes())));

        // then
        assertThat(exc).isNull();
        verify(insights).trackFtpUpload(any(Duration.class), eq(true));
        verify(insights, never()).trackException(any(IOException.class));
    }

    @Test
    public void should_thrown_an_exception_if_uploading_file_fails() throws Exception {
        // given
        doThrow(IOException.class).when(sftpFileTransfer).upload(any(LocalSourceFile.class), any());

        // when
        Throwable exc = catchThrowable(() -> client.upload(new PdfDoc("hello.pdf", "hello".getBytes())));

        // then
        assertThat(exc)
            .isInstanceOf(FtpStepException.class)
            .hasMessageContaining("upload");

        verify(insights).trackFtpUpload(any(Duration.class), eq(false));
        verify(insights).trackException(any(IOException.class));
    }

    @Test
    public void should_throw_a_custom_exception_if_downloading_fails() throws Exception {
        // given
        RemoteResourceInfo rri = mock(RemoteResourceInfo.class);
        given(rri.isRegularFile()).willReturn(true);
        given(sftpClient.ls(anyString()))
            .willReturn(singletonList(rri));

        doThrow(IOException.class)
            .when(sftpFileTransfer).download(anyString(), any(InMemoryDownloadedFile.class));

        // when
        Throwable exc = catchThrowable(() -> client.downloadReports());

        // then
        assertThat(exc)
            .isInstanceOf(FtpStepException.class)
            .hasMessageContaining("download");
    }

    @Test
    public void download_should_return_an_empty_list_if_there_are_no_reports() throws Exception {
        // given
        given(sftpClient.ls(anyString()))
            .willReturn(emptyList());

        // when
        List<byte[]> reports = client.downloadReports();

        // then
        assertThat(reports).isEmpty();
    }

    @Test
    public void should_thrown_an_exception_while_getting_sftp_client() throws Exception {
        // given
        reset(sshClient);
        doThrow(IOException.class).when(sshClient).newSFTPClient();

        // when
        Throwable exc = catchThrowable(() -> client.upload(new PdfDoc("hello.pdf", "hello".getBytes())));

        // then
        assertThat(exc).isInstanceOf(FtpStepException.class);
        verify(insights).trackException(any(IOException.class));
    }

    @Test
    public void should_succeed_to_check_health() {
        assertThat(client.isHealthy()).isTrue();
    }

    @Test
    public void should_throw_an_exception_while_getting_sftp_client_for_health_check() throws IOException {
        reset(sshClient);
        doThrow(IOException.class).when(sshClient).newSFTPClient();

        assertThat(client.isHealthy()).isFalse();
    }
}
