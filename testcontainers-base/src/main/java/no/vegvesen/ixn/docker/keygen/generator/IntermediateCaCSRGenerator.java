package no.vegvesen.ixn.docker.keygen.generator;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class IntermediateCaCSRGenerator extends GenericContainer<IntermediateCaCSRGenerator> {



    private static final String KEYS_INTERNAL_FOLDER = "/int_keys";

    private Path keysFolder;
    private String intermediateDomain;
    private String countryCode;

    public IntermediateCaCSRGenerator(ImageFromDockerfile image, Path keysFolder, String intermediateDomain, String countryCode) {
        super(image);
        this.keysFolder = keysFolder;
        this.intermediateDomain = intermediateDomain;
        this.countryCode = countryCode;
    }

    public IntermediateCaCSRGenerator(Path keysFolder, String intermediateDomain, String countryCode) {
        this(new ImageFromDockerfile("intermediate-csr-generator").withFileFromClasspath(".", "intermediateca/csr"),
                keysFolder,
                intermediateDomain,
                countryCode
        );
    }
    @Override
    protected void configure() {
        this.withFileSystemBind(keysFolder.toString(),KEYS_INTERNAL_FOLDER);
        this.withCommand(intermediateDomain,countryCode);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }

    public Path getCsrOnHost() {
        return keysFolder.resolve(String.format("int.%s.csr",intermediateDomain));
    }

    public Path getKeyOnHost() {
        return keysFolder.resolve(String.format("int.%s.key.pem",intermediateDomain));
    }

}
