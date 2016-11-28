package org.zjye.sftp.client;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

import java.io.File;
import java.util.Optional;

public class DefaultSftpClient implements SftpClient {
    private final MessageChannel channel;
    private final PollableChannel pollableChannel;

    public DefaultSftpClient(MessageChannel channel,
                             PollableChannel pollableChannel){
        this.channel = channel;
        this.pollableChannel = pollableChannel;
    }


    @Override
    public void upload(File file) {
        final Message<File> message = MessageBuilder.withPayload(file).build();
        channel.send(message);
    }

    @Override
    public Optional<File> download() {
        Message<?> message = pollableChannel.receive(2000);
        if(message == null) return  Optional.empty();

        return Optional.of((File)message.getPayload());
    }

}
