package org.zjye.sftp.client;

import java.io.File;

public interface SftpClient {
    void upload(File file);
    File download(String filePath);
}
