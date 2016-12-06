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
    public EmbeddedSftpServer embeddedSftpServer() {
        EmbeddedSftpServer embeddedSftpServer = new EmbeddedSftpServer(sftpProperties);
        sftpProperties.setPort(embeddedSftpServer.getPort());
        return embeddedSftpServer;
    }


    @Bean
    public int sftpServerPort(EmbeddedSftpServer embeddedSftpServer) {
        return embeddedSftpServer.getPort();
    }
}
