package org.zjye.sftp;

import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zjye.sftp.configuration.SftpProperties;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {TestContext.class})
public class OutboundTransferTest {

    @Autowired
    SftpProperties sftpProperties;
    @Autowired
    ApplicationContext ac;

    @Test
    public void should_upload_to_remote_folder() throws Exception {
        final String sourceFileName = String.format("%s.txt", UUID.randomUUID().toString());
        final String destinationFileName = sourceFileName + "_foo";

        String sourceFile = Paths.get(sftpProperties.getLocal().getDirectory(), sourceFileName).toString();
        new File(sftpProperties.getLocal().getDirectory()).mkdir();
        assertTrue("failed to create test file", new File(sourceFile).createNewFile());

        @SuppressWarnings("unchecked")
        SessionFactory<ChannelSftp.LsEntry> sessionFactory = ac.getBean(CachingSessionFactory.class);
        RemoteFileTemplate<ChannelSftp.LsEntry> template = new RemoteFileTemplate<>(sessionFactory);
        SftpTestUtils.createTestFiles(template, sftpProperties.getRemote()); // Just the directory

        final File file = new File(sourceFile);

        assertTrue(String.format("File '%s' does not exist.", sourceFile), file.exists());

        final Message<File> message = MessageBuilder.withPayload(file).build();
        final MessageChannel inputChannel = ac.getBean("inputChannel", MessageChannel.class);

        inputChannel.send(message);
        Thread.sleep(2000);

        assertTrue(SftpTestUtils.fileExists(template, sftpProperties.getRemote(), destinationFileName));

        System.out.println(String.format("Successfully transferred '%s' file to a " +
                "remote location under the name '%s'", sourceFileName, destinationFileName));

        SftpTestUtils.cleanUp(template, sftpProperties.getRemote(), destinationFileName);
    }


    @Before
    @After
    public void cleanup() {
        try {
            FileUtils.cleanDirectory(new File(sftpProperties.getLocal().getDirectory()));
            FileUtils.cleanDirectory(new File(sftpProperties.getRemote().getDirectory()));
        } catch (Exception ex) {
            System.out.println("failed to cleanup" + ex.getMessage());
        }
    }
}
