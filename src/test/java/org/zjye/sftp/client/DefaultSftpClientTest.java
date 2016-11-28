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
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zjye.sftp.SftpTestUtils;
import org.zjye.sftp.TestContext;
import org.zjye.sftp.configuration.SftpCommonConfig;
import org.zjye.sftp.configuration.SftpProperties;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.*;


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

        DefaultSftpClient sftpClient = new DefaultSftpClient(
                context.getBean(SftpCommonConfig.OUTPUT_CHANNEL, MessageChannel.class),
                context.getBean(SftpCommonConfig.INPUT_CHANNEL, PollableChannel.class));

        // act
        sftpClient.upload(localFile);

        // assert
        Thread.sleep(2000);
        assertTrue(sftpTestUtils.fileExists(destinationFileName));
    }

    @Test
    public void should_download_file() throws Exception {
        // arrange
        String file1 = String.format("%s.txt", UUID.randomUUID().toString());
        String file2 = String.format("%s.txt", UUID.randomUUID().toString());
        String fileExcluded = String.format("%s.aba", UUID.randomUUID().toString());
        assertFalse("could not delete existing file", new File(sftpProperties.getLocal().getDirectory(), file1).delete());
        assertFalse("could not delete existing file", new File(sftpProperties.getLocal().getDirectory(), file2).delete());

        sftpTestUtils.createTestFiles(file1, file2, fileExcluded);

        DefaultSftpClient sftpClient = new DefaultSftpClient(
                context.getBean(SftpCommonConfig.OUTPUT_CHANNEL, MessageChannel.class),
                context.getBean(SftpCommonConfig.INPUT_CHANNEL, PollableChannel.class));

        List<File> downloadedFiles = new ArrayList<>();

        // act
        Optional<File> file = sftpClient.download();

        while (file.isPresent()) {
            downloadedFiles.add(file.get());
            file = sftpClient.download();
        }

        // assert
        assertEquals(2, downloadedFiles.size());
        assertThat(downloadedFiles.stream().map(File::getName).toArray(), hasItemInArray(file1));
        assertThat(downloadedFiles.stream().map(File::getName).toArray(), hasItemInArray(file2));
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