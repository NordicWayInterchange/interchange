package no.vegvesen.ixn.docker.keygen.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class ServiceProviderCSRGenerator extends GenericContainer<ServiceProviderCSRGenerator> {

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderCSRGenerator.class);

    private static final String KEYS_INTERNAL_FOLDER = "/int_keys";

    private Path keysPath;
    private String username;
    private String countryCode;

    public ServiceProviderCSRGenerator(Path dockerfilePath, Path keysPath, String username, String countryCode) {
       super(new ImageFromDockerfile("sp-csr-generator").withFileFromPath(".",dockerfilePath));
        this.keysPath = keysPath;
        this.username = username;
        this.countryCode = countryCode;
    }

    @Override
    protected void configure() {
        this.withFileSystemBind(keysPath.toString(),KEYS_INTERNAL_FOLDER);
        this.withCommand(username,countryCode);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
        this.withLogConsumer(new Slf4jLogConsumer(logger));
    }

    public Path getKeyOnHost() {
        return keysPath.resolve(String.format("%s.key.pem",username));
    }

    public Path getCsrOnHost() {
        return keysPath.resolve(String.format("%s.csr.pem",username));
    }

}
