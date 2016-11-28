package org.zjye.sftp;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zjye.sftp.configuration.SftpCommonConfig;
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
    ApplicationContext context;
    @Autowired
    SftpTestUtils sftpTestUtils;
    final String testDirectory = "local-test-dir";

    @Test
    public void should_upload_to_remote_folder() throws Exception {
        // arrange
        final String sourceFileName = String.format("%s.txt", UUID.randomUUID().toString());
        final String destinationFileName = sourceFileName + "_foo";

        String sourceFile = Paths.get(testDirectory, sourceFileName).toString();
        new File(testDirectory).mkdir();
        assertTrue("failed to create test file", new File(sourceFile).createNewFile());

        sftpTestUtils.createTestFiles(); // Just the directory

        final File file = new File(sourceFile);

        assertTrue(String.format("File '%s' does not exist.", sourceFile), file.exists());

        final Message<File> message = MessageBuilder.withPayload(file).build();
        final MessageChannel inputChannel = context.getBean(SftpCommonConfig.OUTPUT_CHANNEL, MessageChannel.class);

        // act
        inputChannel.send(message);
        Thread.sleep(2000);

        // assert
        assertTrue(sftpTestUtils.fileExists(destinationFileName));

        System.out.println(String.format("Successfully transferred '%s' file to a " +
                "remote location under the name '%s'", sourceFileName, destinationFileName));

        sftpTestUtils.cleanUp(destinationFileName);
    }


    @Before
    @After
    public void cleanup() {
        try {
            FileUtils.cleanDirectory(new File(testDirectory));
            FileUtils.cleanDirectory(new File(sftpProperties.getLocal().getDirectory()));
            FileUtils.cleanDirectory(new File(sftpProperties.getRemote().getDirectory()));
        } catch (Exception ex) {
            System.out.println("failed to cleanup" + ex.getMessage());
        }
    }
}
