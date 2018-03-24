package uk.gov.hmcts.reform.sendletter;

import com.google.common.collect.ImmutableMap;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(SpringRunner.class)
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
@TestPropertySource("classpath:application.properties")
public abstract class FunctionalTestSuite {

    protected static final String SMOKE_TEST_LETTER_TYPE = "smoke_test";

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${producer-url}")
    protected String producerUrl;

    @Value("${ftp-hostname}")
    protected String ftpHostname;

    @Value("${ftp-port}")
    protected Integer ftpPort;

    @Value("${ftp-fingerprint}")
    protected String ftpFingerprint;

    @Value("${ftp-target-folder}")
    protected String ftpTargetFolder;

    @Value("${ftp-user}")
    protected String ftpUser;

    @Value("${ftp-private-key}")
    protected String ftpPrivateKey;

    @Value("${ftp-public-key}")
    protected String ftpPublicKey;

    @Value("${max-wait-for-ftp-file-in-ms}")
    protected int maxWaitForFtpFileInMs;


    /**
     * Sign in to s2s.
     *
     * @return s2s JWT token.
     */
    protected String signIn() {
        Map<String, Object> params = ImmutableMap.of(
            "microservice", this.s2sName,
            "oneTimePassword", new GoogleAuthenticator().getTotpPassword(this.s2sSecret)
        );

        Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(this.s2sUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(params)
            .post("/lease")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        return response
            .getBody()
            .print();
    }

    protected String sendPrintLetterRequest(String jwt, String jsonBody) throws JSONException {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + jwt)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .baseUri(this.producerUrl)
            .body(jsonBody)
            .when()
            .post("/letters")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .get("letter_id");
    }

    protected String sampleLetterRequestJson(int numberOfDocuments) throws JSONException {
        return new JSONObject()
            .put("type", SMOKE_TEST_LETTER_TYPE)
            .put("documents", createDocumentJsonArray(numberOfDocuments))
            .toString();
    }

    protected SFTPClient getSftpClient() throws IOException {
        SSHClient ssh = new SSHClient();

        ssh.addHostKeyVerifier(ftpFingerprint);
        ssh.connect(ftpHostname, ftpPort);

        ssh.authPublickey(
            ftpUser,
            ssh.loadKeys(ftpPrivateKey, ftpPublicKey, null)
        );

        return ssh.newSFTPClient();
    }

    protected ZipInputStream getZipInputStream(RemoteFile zipFile) throws IOException {
        byte[] fileContent = new byte[(int) zipFile.length()];
        zipFile.read(0, fileContent, 0, (int) zipFile.length());

        ByteArrayInputStream inputStream =
            new ByteArrayInputStream(fileContent, 0, fileContent.length);

        return new ZipInputStream(inputStream);
    }

    protected byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[10000];
        int len;

        while ((len = input.read(buffer)) > 0) {
            output.write(buffer, 0, len);
        }

        return output.toByteArray();
    }

    protected String getPdfFileNamePattern(String letterId) {
        return String.format(
            "%s_%s_%s.pdf",
            Pattern.quote(SMOKE_TEST_LETTER_TYPE),
            Pattern.quote(s2sName.replace("_", "")),
            Pattern.quote(letterId)
        );
    }

    protected String getZipFileNamePattern(String letterId) {
        return String.format(
            "%s_%s_\\d{14}_%s.zip",
            Pattern.quote(SMOKE_TEST_LETTER_TYPE),
            Pattern.quote(s2sName.replace("_", "")),
            Pattern.quote(letterId)
        );
    }

    private JSONArray createDocumentJsonArray(int numberOfDocuments) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        Stream
            .generate(this::createJsonDocument)
            .limit(numberOfDocuments)
            .forEach(jsonArray::put);

        return jsonArray;
    }

    private JSONObject createJsonDocument() {
        try {
            return new JSONObject()
                .put("template", "<html>hello {{uuid}}</html>")
                .put("values", new JSONObject()
                    // so that we don't send the same letter twice
                    .put("uuid", UUID.randomUUID().toString())
                );
        } catch (JSONException exc) {
            throw new RuntimeException("Failed to create document JSON", exc);
        }
    }
}
