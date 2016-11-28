package org.zjye.sftp.configuration;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "sftp")
public class SftpProperties {
    private String host;
    private int port;
    private String username;
    private PrivateKey privateKey = new PrivateKey();
    private RemoteFolderSettings remote = new RemoteFolderSettings();
    private LocalFolderSettings local = new LocalFolderSettings();

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

    public LocalFolderSettings getLocal() {
        return local;
    }

    public void setLocal(LocalFolderSettings local) {
        this.local = local;
    }

    public RemoteFolderSettings getRemote() {
        return remote;
    }

    public void setRemote(RemoteFolderSettings remote) {
        this.remote = remote;
    }


    public static class RemoteFolderSettings extends FolderSettings {
        public RemoteFolderSettings() {
            this.outputFileName = "payload.getName()";
        }
    }

    public static class LocalFolderSettings extends FolderSettings {
        public LocalFolderSettings() {
            this.outputFileName = "#this";
        }
    }

    public static class FolderSettings {
        private String directory;
        private String filter = ".*";
        protected String outputFileName;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public String getOutputFileName() {
            return outputFileName;
        }

        public void setOutputFileName(String outputFileName) {
            this.outputFileName = outputFileName;
        }
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
