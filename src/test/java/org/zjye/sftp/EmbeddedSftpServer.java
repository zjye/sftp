/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zjye.sftp;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.util.Base64;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.SocketUtils;
import org.springframework.util.StreamUtils;
import org.zjye.sftp.configuration.SftpProperties;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;

public class EmbeddedSftpServer implements InitializingBean, SmartLifecycle {

    private final SftpProperties sftpProperties;

    public EmbeddedSftpServer(SftpProperties sftpProperties) {
        this.sftpProperties = sftpProperties;
    }

    public int getPort() {
        if(port == 0) {
            port = SocketUtils.findAvailableTcpPort();
        }

        return port;
    }


	private final SshServer server = SshServer.setUpDefaultServer();

	private volatile int port;

	private volatile boolean running;

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		final PublicKey allowedKey = decodePublicKey();
		this.server.setPublickeyAuthenticator((username, key, session) -> key.equals(allowedKey));
		this.server.setPort(getPort());
		this.server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		this.server.setSubsystemFactories(Collections.singletonList(new SftpSubsystem.Factory()));
		final String virtualDir = new FileSystemResource("").getFile().getAbsolutePath();
		server.setFileSystemFactory(new VirtualFileSystemFactory(virtualDir));
	}

	private PublicKey decodePublicKey() throws Exception {
		InputStream stream = new ClassPathResource(((ClassPathResource) this.sftpProperties.getPrivateKey().getFile()).getPath() + ".pub").getInputStream();
		byte[] decodeBuffer = Base64.decodeBase64(StreamUtils.copyToByteArray(stream));
		ByteBuffer bb = ByteBuffer.wrap(decodeBuffer);
		int len = bb.getInt();
		byte[] type = new byte[len];
		bb.get(type);
		if ("ssh-rsa".equals(new String(type))) {
			BigInteger e = decodeBigInt(bb);
			BigInteger m = decodeBigInt(bb);
			RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
			return KeyFactory.getInstance("RSA").generatePublic(spec);

		}
		else {
			throw new IllegalArgumentException("Only supports RSA");
		}
	}

	private BigInteger decodeBigInt(ByteBuffer bb) {
		int len = bb.getInt();
		byte[] bytes = new byte[len];
		bb.get(bytes);
		return new BigInteger(bytes);
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void start() {
		try {
			server.start();
			this.running  = true;
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public void stop() {
		if (this.running) {
			try {
				server.stop(true);
			}
			catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
			finally {
				this.running = false;
			}
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

}
