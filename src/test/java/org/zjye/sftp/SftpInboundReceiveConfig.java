package org.zjye.sftp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.File;

@Configuration
@ImportResource({"/META-INF/spring/integration/SftpInboundReceiveSample-context-config.xml"})
//@EnableIntegration
@EnableConfigurationProperties({SftpProperties.class})
public class SftpInboundReceiveConfig {
//
    @Autowired
    SftpProperties sftpProperties;
//
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
    public CachingSessionFactory sftpSessionFactory() {
        return new CachingSessionFactory(defaultSftpSessionFactory());
    }
//
//
//    @Bean
//    @InboundChannelAdapter(value = "sftpInbondAdapter", autoStartup = "false", poller = {
//            @Poller(fixedRate = "1000", maxMessagesPerPoll = "1")
//    })
//    public MessageSource<File> sftpMessageSource() {
//        SftpInboundFileSynchronizingMessageSource source =
//                new SftpInboundFileSynchronizingMessageSource(sftpInboundFileSynchronizer());
//        source.setLocalDirectory(new File("local-dir"));
//        source.setAutoCreateLocalDirectory(true);
//        source.setLocalFilter(new AcceptOnceFileListFilter<>());
//        source.setAutoCreateLocalDirectory(true);
//        return source;
//    }
//
//    @Bean
//    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer() {
//        SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(defaultSftpSessionFactory());
//        fileSynchronizer.setDeleteRemoteFiles(false);
//        fileSynchronizer.setRemoteDirectory("si.sftp.sample");
//        fileSynchronizer.setDeleteRemoteFiles(false);
//        fileSynchronizer.setFilter(new SftpSimplePatternFileListFilter(".*\\.txt$"));
//        return fileSynchronizer;
//    }
//
//

//
    @Bean
    public MessageChannel receiveChannel() {
        return new QueueChannel();
    }

}
