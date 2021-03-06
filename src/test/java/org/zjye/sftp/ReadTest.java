package org.zjye.sftp;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class ReadTest {

    @Test
    public void runDemo(){

        ConfigurableApplicationContext context =
                new ClassPathXmlApplicationContext("/META-INF/spring/integration/SftpInboundReceiveSample-context.xml", this.getClass());
        RemoteFileTemplate<LsEntry> template = null;
        String file1 = "a.txt";
        String file2 = "b.txt";
        String file3 = "c.bar";
        new File("local-dir", file1).delete();
        new File("local-dir", file2).delete();
        try {
            PollableChannel localFileChannel = context.getBean("receiveChannel", PollableChannel.class);
            @SuppressWarnings("unchecked")
            SessionFactory<LsEntry> sessionFactory = context.getBean(CachingSessionFactory.class);
            template = new RemoteFileTemplate<LsEntry>(sessionFactory);
            SftpTestUtils.createTestFiles(template, file1, file2, file3);

            SourcePollingChannelAdapter adapter = context.getBean(SourcePollingChannelAdapter.class);
            adapter.start();

            Message<?> received = localFileChannel.receive();
            assertNotNull("Expected file", received);
            System.out.println("Received first file message: " + received);
            received = localFileChannel.receive();
            assertNotNull("Expected file", received);
            System.out.println("Received second file message: " + received);
            received = localFileChannel.receive(1000);
            assertNull("Expected null", received);
            System.out.println("No third file was received as expected");
        }
        finally {
            SftpTestUtils.cleanUp(template, file1, file2, file3);
            assertTrue("Could note delete retrieved file", new File("local-dir", file1).delete());
            assertTrue("Could note delete retrieved file", new File("local-dir", file2).delete());
        }
    }

}

