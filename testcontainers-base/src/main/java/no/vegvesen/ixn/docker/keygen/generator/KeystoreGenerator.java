package no.vegvesen.ixn.docker.keygen.generator;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class KeystoreGenerator extends GenericContainer<KeystoreGenerator> {
    private Path keyOnHost;
    private Path chainCertOnHost;
    private String domainName;
    private Path caCertOnHost;
    private String password;
    private Path outputFile;

    public KeystoreGenerator(ImageFromDockerfile image,
                             Path keyOnHost,
                             Path chainCertOnHost,
                             String domainName,
                             Path caCertOnHost,
                             String password,
                             Path outputFile) {
        super(image);
        this.keyOnHost = keyOnHost;
        this.chainCertOnHost = chainCertOnHost;
        this.domainName = domainName;
        this.caCertOnHost = caCertOnHost;
        this.password = password;
        this.outputFile = outputFile;
    }

    public KeystoreGenerator(Path keyOnHost,
                             Path chainCertOnHost,
                             String domainName,
                             Path caCertOnHost,
                             String password,
                             Path outputFile) {

        this(new ImageFromDockerfile("keystore-generator")
                .withFileFromClasspath(".", "keystores"),
                keyOnHost,
                chainCertOnHost,
                domainName,
                caCertOnHost,
                password,
                outputFile);
    }

        @Override
    protected void configure() {
        this.withFileSystemBind(keyOnHost.toString(), "/keys/in-key");
        this.withFileSystemBind(chainCertOnHost.toString(), "/chain/cert");
        this.withFileSystemBind(caCertOnHost.toString(), "/ca/cert");
        this.withFileSystemBind(outputFile.getParent().toString(), "/out/");
        String outputFileInContainer = "/out/" + outputFile.getFileName();
        this.withCommand("/keys/in-key", "/chain/cert", domainName, "/ca/cert", password, outputFileInContainer);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }
}
