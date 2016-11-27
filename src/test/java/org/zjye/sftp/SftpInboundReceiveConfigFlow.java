package org.zjye.sftp;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.PollerSpec;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

import java.io.File;
import java.util.concurrent.TimeUnit;


@EnableIntegration
@EnableConfigurationProperties({SftpProperties.class})
public class SftpInboundReceiveConfigFlow {

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
