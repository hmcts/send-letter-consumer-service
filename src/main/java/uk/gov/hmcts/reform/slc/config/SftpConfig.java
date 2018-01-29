package uk.gov.hmcts.reform.slc.config;

import net.schmizz.sshj.SSHClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SftpConfig {

    @Bean
    public SSHClient sshClient() {
        return new SSHClient();
    }
}
