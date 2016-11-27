package org.zjye.sftp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SftpProperties.class})
public class SftpCommonConfig {
    @Autowired
    SftpProperties sftpProperties;

    @Bean
    public int serverPort() {
        if (sftpProperties.getPort() > 0) return sftpProperties.getPort();

        return EmbeddedSftpServer.PORT;
    }

    @Bean
    public EmbeddedSftpServer embeddedSftpServer() {
        EmbeddedSftpServer embeddedSftpServer = new EmbeddedSftpServer();
        embeddedSftpServer.setPort(serverPort());
        return embeddedSftpServer;
    }
}
