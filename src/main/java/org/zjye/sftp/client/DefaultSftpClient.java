package org.zjye.sftp.client;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.File;

public class DefaultSftpClient implements SftpClient {
    private final MessageChannel channel;

    public DefaultSftpClient(MessageChannel channel){
        this.channel = channel;
    }

    @Override
    public void upload(File file) {
        final Message<File> message = MessageBuilder.withPayload(file).build();
        channel.send(message);
    }

    @Override
    public File download(String filePath) {
        return null;
    }
}
