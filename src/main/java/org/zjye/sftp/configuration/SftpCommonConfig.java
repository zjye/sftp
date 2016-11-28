package org.zjye.sftp.configuration;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.zjye.sftp.client.DefaultSftpClient;
import org.zjye.sftp.client.SftpClient;

@Configuration
@EnableIntegration
public class SftpCommonConfig {
    public final static String INPUT_CHANNEL = "inputChannel";
    public final static String OUTPUT_CHANNEL = "outputChannel";

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

    @Bean
    @DependsOn({"sftpInboundFlow", "sftpOutboundFlow"})
    public SftpClient sftpClient(MessageChannel outputChannel, PollableChannel inputChannel) {
        return new DefaultSftpClient(outputChannel, inputChannel);
    }
}
