package org.zjye.sftp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zjye.sftp.configuration.SftpCommonConfig;
import org.zjye.sftp.configuration.SftpProperties;

import java.io.File;
import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {TestContext.class})
public class InboundReceiveTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ApplicationContext context;

    @Autowired
    SftpProperties sftpProperties;

    @Autowired
    SftpTestUtils sftpTestUtils;

    @Test
    public void should_receive_file() throws Exception {
        // arrange
        String file1 = String.format("%s.txt", UUID.randomUUID().toString());
        String file2 = String.format("%s.txt", UUID.randomUUID().toString());
        String fileExcluded = String.format("%s.aba", UUID.randomUUID().toString());
        assertFalse("could not delete existing file", new File(sftpProperties.getLocal().getDirectory(), file1).delete());
        assertFalse("could not delete existing file", new File(sftpProperties.getLocal().getDirectory(), file2).delete());

        PollableChannel localFileChannel = context.getBean(SftpCommonConfig.INPUT_CHANNEL, PollableChannel.class);
        sftpTestUtils.createTestFiles(file1, file2, fileExcluded);

        Thread.sleep(1000);

        // act
        Message<?> received = localFileChannel.receive();

        // assert
        assertNotNull(received);
        int count = 1;
        while (received != null) {
            System.out.println("Received file message: " + received);
            assertThat(received.getPayload(), instanceOf(File.class));
            File file = (File) received.getPayload();
            assertNotEquals("aba", FilenameUtils.getExtension(file.getName()));
            assertEquals("txt", FilenameUtils.getExtension(file.getName()));
            assertTrue("Could not delete retrieved file", file.delete());
            received = localFileChannel.receive(2000);
            if (received != null) count++;
        }

        assertEquals(2, count);
    }

    @Before
    @After
    public void cleanup() {
        try {
            FileUtils.cleanDirectory(new File(sftpProperties.getLocal().getDirectory()));
            FileUtils.cleanDirectory(new File(sftpProperties.getRemote().getDirectory()));
        } catch (Exception ex) {
            logger.error("failed to cleanup", ex);
        }
    }

}

