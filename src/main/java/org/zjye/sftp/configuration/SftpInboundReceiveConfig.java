package org.zjye.sftp.configuration;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import java.io.File;

@Configuration
@EnableIntegration
@EnableConfigurationProperties({SftpProperties.class})
public class SftpInboundReceiveConfig {

    @Autowired
    SftpProperties sftpProperties;

    @Autowired
    int serverPort;

    @Bean
    public DefaultSftpSessionFactory defaultSftpSessionFactory() {
        DefaultSftpSessionFactory defaultSftpSessionFactory = new DefaultSftpSessionFactory();
        defaultSftpSessionFactory.setHost(sftpProperties.getHost());
        defaultSftpSessionFactory.setPrivateKey(sftpProperties.getPrivateKey().getFile());
        defaultSftpSessionFactory.setPrivateKeyPassphrase(sftpProperties.getPrivateKey().getPassphrase());
        defaultSftpSessionFactory.setUser(sftpProperties.getUsername());
        defaultSftpSessionFactory.setPort(serverPort);
        defaultSftpSessionFactory.setAllowUnknownKeys(true);
        return defaultSftpSessionFactory;
    }

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        return new CachingSessionFactory(defaultSftpSessionFactory());
    }

    @Bean
    public IntegrationFlow sftpInboundFlow() {
        return IntegrationFlows
                .from(s -> s.sftp(sftpSessionFactory())
                                .preserveTimestamp(true)
                                .remoteDirectory("si.sftp.sample")
                                .regexFilter(".*\\.txt$")
                                .localDirectory(new File("local-dir"))
                                .autoCreateLocalDirectory(true)
                                .deleteRemoteFiles(false)
                        ,
                        e -> e.id("sftpInboundAdapter")
                                .autoStartup(false)
                                .poller(Pollers
                                        .fixedRate(1000)
                                        .maxMessagesPerPoll(1)
                                ))
                .channel(MessageChannels.queue("receiveChannel"))
                .get();
    }
}
