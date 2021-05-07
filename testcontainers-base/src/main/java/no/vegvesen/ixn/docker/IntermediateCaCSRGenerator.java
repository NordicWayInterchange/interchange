package no.vegvesen.ixn.docker;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;

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
        this.waitingFor(Wait.forLogMessage(".*Certificate Signing Request file created.*",1));

    }
}
