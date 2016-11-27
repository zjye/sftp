package org.zjye.sftp;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan
@Import(
        {
//                SftpInboundReceiveConfigXml.class,
//                SftpInboundReceiveConfig.class,
                SftpInboundReceiveConfigFlow.class
        }
)
public class TestContext {
}
