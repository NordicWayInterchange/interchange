package no.vegvesen.ixn.docker.keygen.generator;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class TruststoreGenerator extends GenericContainer<TruststoreGenerator> {
    private Path caCertPath;
    private String password;
    private Path targetPath;

    public TruststoreGenerator(Path dockerFilePath, Path caCertPath, String password, Path outputFile) {
        super(new ImageFromDockerfile("truststore-generator")
                .withFileFromPath(".",dockerFilePath));
        this.caCertPath = caCertPath;
        this.password = password;
        this.targetPath = outputFile;
    }

    @Override
    protected void configure() {
        this.withFileSystemBind(caCertPath.getParent().toString(),"/ca-certs");
        this.withFileSystemBind(targetPath.getParent().toString(),"/out");
        String certPathInContainer = "/ca-certs/" + caCertPath.getFileName();
        String keystoreInContainer = "/out/" + targetPath.getFileName();
        this.withCommand(certPathInContainer, keystoreInContainer, password);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }
}
