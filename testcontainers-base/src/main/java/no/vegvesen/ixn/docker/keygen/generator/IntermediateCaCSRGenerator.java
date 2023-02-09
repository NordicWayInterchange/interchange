package no.vegvesen.ixn.docker.keygen.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class IntermediateCaCSRGenerator extends GenericContainer<IntermediateCaCSRGenerator> {


    private static Logger logger = LoggerFactory.getLogger(IntermediateCaCSRGenerator.class);

    private static final String KEYS_INTERNAL_FOLDER = "/int_keys";

    private Path keysFolder;
    private String intermediateDomain;
    private String countryCode;

    public IntermediateCaCSRGenerator(Path dockerFilePath, Path keysFolder, String intermediateDomain, String countryCode) {
        super(new ImageFromDockerfile("intermediate-csr-generator").withFileFromPath(".", dockerFilePath));
        this.keysFolder = keysFolder;
        this.intermediateDomain = intermediateDomain;
        this.countryCode = countryCode;
    }

    @Override
    protected void configure() {
        this.withFileSystemBind(keysFolder.toString(),KEYS_INTERNAL_FOLDER);
        this.withCommand(intermediateDomain,countryCode);
        this.withLogConsumer(new Slf4jLogConsumer(logger));
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }

    public Path getCsrOnHost() {
        return keysFolder.resolve(String.format("int.%s.csr",intermediateDomain));
    }

    public Path getKeyOnHost() {
        return keysFolder.resolve(String.format("int.%s.key.pem",intermediateDomain));
    }

}
