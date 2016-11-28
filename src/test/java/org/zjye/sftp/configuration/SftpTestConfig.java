package org.zjye.sftp.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zjye.sftp.EmbeddedSftpServer;

@Configuration
@EnableConfigurationProperties({SftpProperties.class})
public class SftpTestConfig {
    @Autowired
    SftpProperties sftpProperties;

    @Bean
    public EmbeddedSftpServer embeddedSftpServer(int sftpServerPort) {
        EmbeddedSftpServer embeddedSftpServer = new EmbeddedSftpServer(sftpProperties);
        embeddedSftpServer.setPort(sftpServerPort);
        return embeddedSftpServer;
    }

    @Bean
    public int sftpServerPort() {
        if (sftpProperties.getPort() > 0) return sftpProperties.getPort();

        return EmbeddedSftpServer.PORT;
    }
}
