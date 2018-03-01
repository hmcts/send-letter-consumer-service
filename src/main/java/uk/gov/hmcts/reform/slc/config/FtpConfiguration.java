package uk.gov.hmcts.reform.slc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.slc.model.FtpConfigProperties;

@Configuration
public class FtpConfiguration {

    @Value("${ftp.hostname}")
    private String hostname;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.fingerprint}")
    private String fingerprint;

    @Value("${ftp.user}")
    private String username;

    @Value("${ftp.keys.public}")
    private String publicKey;

    @Value("${ftp.keys.private}")
    private String privateKey;

    @Value("${ftp.target-folder}")
    private String targetFolder;

    @Value("${ftp.reports-folder}")
    private String reportsFolder;

    @Bean
    public FtpConfigProperties configProperties() {
        return FtpConfigProperties
            .builder()
            .hostname(hostname)
            .port(port)
            .fingerprint(fingerprint)
            .username(username)
            .publicKey(publicKey)
            .privateKey(privateKey)
            .targetFolder(targetFolder)
            .reportsFolder(reportsFolder)
            .build();
    }
}
