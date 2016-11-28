package org.zjye.sftp.client;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zjye.sftp.SftpTestUtils;
import org.zjye.sftp.TestContext;
import org.zjye.sftp.configuration.SftpProperties;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {TestContext.class})
public class DefaultSftpClientTest {

    @Autowired
    SftpProperties sftpProperties;
    @Autowired
    SftpTestUtils sftpTestUtils;
    @Autowired
    ApplicationContext context;

    final String testDirectory = "local-test-dir";

    @Test
    public void should_upload_file() throws Exception {
        // arrange
        final String sourceFileName = String.format("%s.txt", UUID.randomUUID().toString());
        final String destinationFileName = sourceFileName + "_foo";

        String sourceFile = Paths.get(testDirectory, sourceFileName).toString();
        new File(testDirectory).mkdir();
        File localFile = new File(sourceFile);

        assertTrue("failed to create test file", localFile.createNewFile());

        DefaultSftpClient sftpClient = new DefaultSftpClient(context.getBean("inputChannel", MessageChannel.class));

        // act
        sftpClient.upload(localFile);

        // assert
        Thread.sleep(2000);
        assertTrue(sftpTestUtils.fileExists(destinationFileName));
    }


    @Before
    @After
    public void cleanup() {
        try {
            FileUtils.cleanDirectory(new File(testDirectory));
            FileUtils.cleanDirectory(new File(sftpProperties.getRemote().getDirectory()));
        } catch (Exception ex) {
            System.out.println("failed to cleanup" + ex.getMessage());
        }
    }
}