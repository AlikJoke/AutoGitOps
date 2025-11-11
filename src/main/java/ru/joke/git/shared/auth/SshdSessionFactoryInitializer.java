package ru.joke.git.shared.auth;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.ServerKeyDatabase;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.util.FS;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

public class SshdSessionFactoryInitializer {

    public void initialize(
            final String privateKeyFilePath,
            final String passPhrase,
            final boolean useLegacyKexAlgorithms
    ) throws IOException, GeneralSecurityException {
        final var privateKeyPath = Path.of(privateKeyFilePath);

        final var sshSessionFactory =
                useLegacyKexAlgorithms
                        ? createSshSessionFactoryWithLegacyKexSupport(privateKeyPath, passPhrase)
                        : createDefaultSshSessionFactory(privateKeyPath, passPhrase);

        SshdSessionFactory.setInstance(sshSessionFactory);
    }

    private SshSessionFactory createSshSessionFactoryWithLegacyKexSupport(
            final Path privateKeyPath,
            final String passPhrase
    ) {
        return new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
                JSch jsch = super.getJSch(hc, fs);

                if (passPhrase == null || passPhrase.isEmpty()) {
                    jsch.addIdentity(privateKeyPath.toString());
                } else {
                    try {
                        byte[] prv = Files.readAllBytes(privateKeyPath);
                        byte[] pass = passPhrase.getBytes(StandardCharsets.UTF_8);
                        jsch.addIdentity("key-from-openssh", prv, null, pass);
                    } catch (IOException e) {
                        throw new JSchException("Unable to read private key", e);
                    }
                }

                return jsch;
            }
        };
    }

    private SshSessionFactory createDefaultSshSessionFactory(
            final Path privateKeyPath,
            final String passPhrase
    ) throws IOException, GeneralSecurityException {

        final var keyPairs = SecurityUtils.loadKeyPairIdentities(
                null,
                null,
                new ByteArrayInputStream(Files.readAllBytes(privateKeyPath)),
                (session, resourceKey, retryIndex) -> passPhrase
        );

        final var sshDirectory = Files.createTempDirectory(".ssh").toFile();
        sshDirectory.deleteOnExit();

        return new SshdSessionFactoryBuilder()
                .setPreferredAuthentications("publickey")
                .setDefaultKeysProvider(dir -> keyPairs)
                .setHomeDirectory(FS.detect().userHome())
                .setSshDirectory(sshDirectory)
                .setServerKeyDatabase((homeDir, sshDir) -> new ServerKeyDatabase() {
                    @Override
                    public List<PublicKey> lookup(String connectAddress, InetSocketAddress remoteAddress, Configuration config) {
                        return Collections.emptyList();
                    }

                    @Override
                    public boolean accept(String connectAddress, InetSocketAddress remoteAddress,
                            PublicKey serverKey, Configuration config, CredentialsProvider provider) {
                        return true;
                    }
                })
                .build(new JGitKeyCache());
    }
}
