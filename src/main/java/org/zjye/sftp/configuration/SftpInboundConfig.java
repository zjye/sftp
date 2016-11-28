package org.zjye.sftp.configuration;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.SessionFactory;

import java.io.File;

@Configuration
@EnableConfigurationProperties({SftpProperties.class})
public class SftpInboundConfig {

    @Autowired
    SftpProperties sftpProperties;


    @Bean
    public IntegrationFlow sftpInboundFlow(SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory) {
        return IntegrationFlows
                .from(s -> s.sftp(sftpSessionFactory)
                                .preserveTimestamp(true)
                                .remoteDirectory(sftpProperties.getRemote().getDirectory())
                                .regexFilter(sftpProperties.getRemote().getFilter())
                                .localDirectory(new File(sftpProperties.getLocal().getDirectory()))
                                .localFilenameExpression(sftpProperties.getLocal().getOutputFileName())
                                .autoCreateLocalDirectory(true)
                                .deleteRemoteFiles(false)
                                .filter(new AcceptOnceFileListFilter<>())
                        ,
                        e -> e.id("sftpInboundAdapter")
                                .autoStartup(true)
                                .poller(Pollers
                                        .fixedRate(1000)
                                        .maxMessagesPerPoll(1)
                                ))
                .channel(MessageChannels.queue(SftpCommonConfig.INPUT_CHANNEL))
                .get();
    }
}
