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
import uk.gov.hmcts.reform.slc.config.FtpConfigProperties;
import uk.gov.hmcts.reform.slc.logging.AppInsights;
import uk.gov.hmcts.reform.slc.services.steps.sftpupload.exceptions.FtpStepException;
import uk.gov.hmcts.reform.slc.services.steps.zip.ZippedDoc;

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
import static org.mockito.Matchers.contains;
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
    @Mock private FtpConfigProperties configProperties;

    private FtpClient client;

    @Before
    public void setUp() throws Exception {
        given(sshClient.newSFTPClient()).willReturn(sftpClient);
        given(sftpClient.getFileTransfer()).willReturn(sftpFileTransfer);

        client = new FtpClient(
            () -> sshClient,
            configProperties
        );

        ReflectionTestUtils.setField(client, "insights", insights);
    }

    @Test
    public void should_not_throw_an_exception_if_closing_connection_fails() throws Exception {
        // given
        doThrow(IOException.class).when(sshClient).close();

        // when
        Throwable exc = catchThrowable(() -> client.upload(sampleFileToUpload(), false));

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
        Throwable exc = catchThrowable(() -> client.upload(sampleFileToUpload(), false));

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
    public void should_throw_a_custom_exception_if_listing_folder_contents_fails() throws Exception {
        // given
        doThrow(IOException.class)
            .when(sftpClient).ls(anyString());

        // when
        Throwable exc = catchThrowable(() -> client.downloadReports());

        // then
        assertThat(exc)
            .isInstanceOf(FtpStepException.class)
            .hasMessageContaining("Error while downloading reports");
    }

    @Test
    public void download_should_return_an_empty_list_if_there_are_no_reports() throws Exception {
        // given
        given(sftpClient.ls(anyString()))
            .willReturn(emptyList());

        // when
        List<Report> reports = client.downloadReports();

        // then
        assertThat(reports).isEmpty();
    }

    @Test
    public void should_thrown_an_exception_while_getting_sftp_client() throws Exception {
        // given
        reset(sshClient);
        doThrow(IOException.class).when(sshClient).newSFTPClient();

        // when
        Throwable exc = catchThrowable(() -> client.upload(sampleFileToUpload(), false));

        // then
        assertThat(exc).isInstanceOf(FtpStepException.class);
        verify(insights).trackException(any(IOException.class));
    }

    @Test
    public void testConnection_should_not_throw_an_exception_when_connection_can_be_established() {
        Throwable exc = catchThrowable(() -> client.testConnection());

        assertThat(exc).isNull();
    }

    @Test
    public void testConnection_should_throw_an_exception_when_unable_to_connect() throws IOException {
        reset(sshClient);
        doThrow(IOException.class).when(sshClient).newSFTPClient();

        Throwable exc = catchThrowable(() -> client.testConnection());

        assertThat(exc).isNotNull();
    }

    @Test
    public void deleteReports_should_throw_an_exception_if_deleting_file_failed() throws Exception {
        // given
        doThrow(IOException.class)
            .when(sftpClient).rm("hello.csv");

        // when
        Throwable exc = catchThrowable(() -> client.deleteReport("hello.csv"));

        // then
        assertThat(exc)
            .isInstanceOf(FtpStepException.class)
            .hasMessageContaining("Error while deleting report");
    }

    @Test
    public void should_upload_to_special_folder_if_letter_is_a_smoke_test_letter() throws Exception {
        given(configProperties.getSmokeTestTargetFolder()).willReturn("smoke");
        given(configProperties.getTargetFolder()).willReturn("target");

        client.upload(sampleFileToUpload(), true);

        verify(sftpFileTransfer)
            .upload(
                any(LocalSourceFile.class),
                contains("smoke")
            );
    }

    @Test
    public void should_upload_to_target_folder_if_letter_is_not_a_smoke_test_letter() throws Exception {
        given(configProperties.getSmokeTestTargetFolder()).willReturn("smoke");
        given(configProperties.getTargetFolder()).willReturn("target");

        client.upload(sampleFileToUpload(), false);

        verify(sftpFileTransfer)
            .upload(
                any(LocalSourceFile.class),
                contains("target")
            );
    }

    private ZippedDoc sampleFileToUpload() {
        return new ZippedDoc("hello.zip", "hello".getBytes());
    }
}
