/*
 * Copyright 2014 the original author or authors.
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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.SessionCallback;
import org.zjye.sftp.configuration.SftpProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SftpTestUtils {

	static void createTestFiles(RemoteFileTemplate<LsEntry> template, SftpProperties.RemoteFolderSettings remoteFolder, final String... fileNames) {
		if (template != null) {
			template.execute((SessionCallback<LsEntry, Void>) session -> {
                try {
                    session.mkdir(remoteFolder.getDirectory());
                }
                catch (Exception e) {
                    assertThat(e.getMessage(), containsString("failed to create"));
                }
				for (String fileName : fileNames) {
                	try(final ByteArrayInputStream stream = new ByteArrayInputStream(fileName.getBytes())) {
                        session.write(stream, remoteFolder.getDirectory() + "/" + fileName);
                    }
				}
                return null;
            });
		}
	}

	static void cleanUp(RemoteFileTemplate<LsEntry> template, SftpProperties.RemoteFolderSettings remoteFolder, final String... fileNames) {
		if (template != null) {
			template.execute((SessionCallback<LsEntry, Void>) session -> {
                // TODO: avoid DFAs with Spring 4.1 (INT-3412)
                ChannelSftp channel = (ChannelSftp) new DirectFieldAccessor(new DirectFieldAccessor(session)
                        .getPropertyValue("targetSession")).getPropertyValue("channel");
				for (String fileName : fileNames) {
					try {
						session.remove(remoteFolder.getDirectory() + "/" + fileName);
					} catch (IOException ignored) {
					}
				}
                try {
                    // should be empty
                    channel.rmdir(remoteFolder.getDirectory());
                }
                catch (SftpException e) {
                    fail("Expected remote directory to be empty " + e.getMessage());
                }
                return null;
            });
		}
	}

	public static boolean fileExists(RemoteFileTemplate<LsEntry> template, SftpProperties.RemoteFolderSettings remoteFolder, final String... fileNames) {
		if (template != null) {
			return template.execute(session -> {
                // TODO: avoid DFAs with Spring 4.1 (INT-3412)
                ChannelSftp channel = (ChannelSftp) new DirectFieldAccessor(new DirectFieldAccessor(session)
                        .getPropertyValue("targetSession")).getPropertyValue("channel");
				for (String fileName : fileNames) {
					try {
						SftpATTRS stat = channel.stat(remoteFolder.getDirectory() + "/" + fileName);
						if (stat == null) {
							System.out.println("stat returned null for " + fileName);
							return false;
						}
					} catch (SftpException e) {
						System.out.println("Remote file not present: " + e.getMessage() + ": " + fileName);
						return false;
					}
				}
                return true;
            });
		}
		else {
			return false;
		}
	}

}
