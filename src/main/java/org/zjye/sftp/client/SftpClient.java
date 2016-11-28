package org.zjye.sftp.client;

import java.io.File;
import java.util.Optional;

public interface SftpClient {
    void upload(File file);
    Optional<File> download();
}
