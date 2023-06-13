package no.vegvesen.ixn.docker.keygen.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class TruststoreGenerator extends GenericContainer<TruststoreGenerator> {
    private Path caCertPath;
    private String password;
    private Path targetPath;

    Logger logger = LoggerFactory.getLogger(TruststoreGenerator.class);

    public TruststoreGenerator(ImageFromDockerfile image, Path caCertPath, String password, Path outputFile) {
        super(image);
        this.caCertPath = caCertPath;
        this.password = password;
        this.targetPath = outputFile;
    }

    public TruststoreGenerator(Path caCertPath, String password, Path outputFile) {
        this(new ImageFromDockerfile("truststore-generator")
                .withFileFromClasspath(".", "truststores"),
                caCertPath,
                password,
                outputFile);
    }
    @Override
    protected void configure() {
        this.withFileSystemBind(caCertPath.getParent().toString(),"/ca-certs");
        this.withFileSystemBind(targetPath.getParent().toString(),"/out");
        String certPathInContainer = "/ca-certs/" + caCertPath.getFileName();
        String keystoreInContainer = "/out/" + targetPath.getFileName();
        this.withCommand(certPathInContainer, keystoreInContainer, password);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
        this.withLogConsumer(new Slf4jLogConsumer(logger));
    }
}
