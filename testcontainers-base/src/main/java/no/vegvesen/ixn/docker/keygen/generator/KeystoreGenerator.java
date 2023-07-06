package no.vegvesen.ixn.docker.keygen.generator;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class KeystoreGenerator extends GenericContainer<KeystoreGenerator> {
    public static final String IN_KEY_PATH = "/keys/in-key";
    public static final String IN_CERT_PATH = "/chain/cert";
    public static final String CA_CERT_PATH = "/ca/cert";
    public static final String OUT_FOLDER = "/out/";
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
        this.withFileSystemBind(keyOnHost.toString(), IN_KEY_PATH);
        this.withFileSystemBind(chainCertOnHost.toString(), IN_CERT_PATH);
        this.withFileSystemBind(caCertOnHost.toString(), CA_CERT_PATH);
        this.withFileSystemBind(outputFile.getParent().toString(), OUT_FOLDER);
        String outputFileInContainer = OUT_FOLDER + outputFile.getFileName();
        this.withCommand(IN_KEY_PATH, IN_CERT_PATH, domainName, CA_CERT_PATH, password, outputFileInContainer);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }
}
