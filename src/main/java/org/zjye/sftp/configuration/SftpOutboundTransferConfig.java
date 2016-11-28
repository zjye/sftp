package org.zjye.sftp.configuration;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;

@Configuration
@EnableConfigurationProperties({SftpProperties.class})
public class SftpOutboundTransferConfig {

    @Autowired
    SftpProperties sftpProperties;

    @Bean
    public IntegrationFlow sftpOutboundFlow(SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory) {

        return IntegrationFlows
                .from("inputChannel")
                .handle(Sftp
                                .outboundAdapter(sftpSessionFactory, FileExistsMode.REPLACE)
                                .fileNameExpression("payload.getName() + '_foo'")
                                .remoteDirectory(sftpProperties.getRemote().getDirectory())
                        ,
                        e -> e.advice(new RequestHandlerRetryAdvice())
                )
                .get();

    }
}
