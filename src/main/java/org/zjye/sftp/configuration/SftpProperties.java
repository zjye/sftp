package org.zjye.sftp.configuration;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "sftp")
public class SftpProperties {
    private String host;
    private int port;
    private String username;
    private PrivateKey privateKey = new PrivateKey();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public static class PrivateKey{
        private Resource file;
        private String passphrase;

        public Resource getFile() {
            return file;
        }

        public void setFile(Resource file) {
            this.file = file;
        }

        public String getPassphrase() {
            return passphrase;
        }

        public void setPassphrase(String passphrase) {
            this.passphrase = passphrase;
        }
    }

}
