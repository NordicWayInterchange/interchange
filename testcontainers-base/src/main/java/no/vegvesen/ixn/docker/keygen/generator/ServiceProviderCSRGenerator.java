package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.docker.DockerBaseIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class ServiceProviderCSRGenerator extends GenericContainer<ServiceProviderCSRGenerator> {


    private static final String KEYS_INTERNAL_FOLDER = "/int_keys";

    private Path keysPath;
    private String username;
    private String countryCode;

    public ServiceProviderCSRGenerator(ImageFromDockerfile image, Path keysPath, String username, String countryCode) {
        super(image);
        this.keysPath = keysPath;
        this.username = username;
        this.countryCode = countryCode;
    }

    public ServiceProviderCSRGenerator(Path keysPath, String username, String countryCode) {
        this(new ImageFromDockerfile("sp-csr-generator").withFileFromClasspath(".", "serviceprovider/csr"),
                keysPath,
                username,
                countryCode);
    }
    @Override
    protected void configure() {
        this.withFileSystemBind(keysPath.toString(),KEYS_INTERNAL_FOLDER);
        this.withCommand(username,countryCode);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }

    public Path getKeyOnHost() {
        return keysPath.resolve(String.format("%s.key.pem",username));
    }

    public Path getCsrOnHost() {
        return keysPath.resolve(String.format("%s.csr.pem",username));
    }


    public CsrKeyPair getCsrKeyPairOnHost() {
        return new CsrKeyPair(getCsrOnHost(),getKeyOnHost());
    }
}
