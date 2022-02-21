package no.vegvesen.ixn.docker;

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
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }

    public Path getCsrOnHost() {
        return keysFolder.resolve(String.format("int.%s.csr",intermediateDomain));
    }

    public Path getKeyOnHost() {
        return keysFolder.resolve(String.format("int.%s.key.pem",intermediateDomain));
    }

    public HostCsrDetail getHostCsrDetails() {
        return new HostCsrDetail(getCsrOnHost(),getKeyOnHost());
    }
}
