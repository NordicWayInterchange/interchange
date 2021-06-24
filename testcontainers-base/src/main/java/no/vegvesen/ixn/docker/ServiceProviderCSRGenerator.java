package no.vegvesen.ixn.docker;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class ServiceProviderCSRGenerator extends GenericContainer<ServiceProviderCSRGenerator> {

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
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(5)));
    }

}
