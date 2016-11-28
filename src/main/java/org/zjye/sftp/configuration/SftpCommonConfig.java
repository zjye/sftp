package org.zjye.sftp.configuration;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Configuration
@EnableIntegration
public class SftpCommonConfig {
    @Autowired
    SftpProperties sftpProperties;
    @Bean
    @ConditionalOnMissingBean
    public int sftpServerPort() {
        return sftpProperties.getPort();
    }

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory(int sftpServerPort) {
        DefaultSftpSessionFactory defaultSftpSessionFactory = new DefaultSftpSessionFactory();
        defaultSftpSessionFactory.setHost(sftpProperties.getHost());
        defaultSftpSessionFactory.setPrivateKey(sftpProperties.getPrivateKey().getFile());
        defaultSftpSessionFactory.setPrivateKeyPassphrase(sftpProperties.getPrivateKey().getPassphrase());
        defaultSftpSessionFactory.setUser(sftpProperties.getUsername());
        defaultSftpSessionFactory.setPort(sftpServerPort);
        defaultSftpSessionFactory.setAllowUnknownKeys(true);
        return new CachingSessionFactory(defaultSftpSessionFactory);
    }
}
