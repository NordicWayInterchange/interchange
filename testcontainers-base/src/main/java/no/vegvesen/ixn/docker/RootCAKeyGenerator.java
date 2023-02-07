package no.vegvesen.ixn.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class RootCAKeyGenerator  extends GenericContainer<RootCAKeyGenerator> {

    private static Logger logger = LoggerFactory.getLogger(RootCAKeyGenerator.class);

    private Path keysFolder;
    private String caDomain;
    private String countryCode;

    public RootCAKeyGenerator(Path dockerFilePath, Path keysFolder, String caDomain, String countryCode) {
        super(new ImageFromDockerfile("vinz-clortho")
                .withFileFromPath(".",dockerFilePath));
        this.keysFolder = keysFolder;
        this.caDomain = caDomain;
        this.countryCode = countryCode;
    }

    @Override
    protected void configure() {
        this.withFileSystemBind(keysFolder.toString(),"/ca_keys");
        this.withCommand(caDomain,countryCode);
        this.withLogConsumer(new Slf4jLogConsumer(logger));
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }

    public Path getKeysFolderOnHost() {
        return keysFolder;
    }

    public Path getCaKeyOnHost() {
        return keysFolder.resolve(String.format("ca.%s.key.pem",caDomain));
    }

    public Path getCaCertOnHost() {
        return keysFolder.resolve(String.format("ca.%s.crt.pem",caDomain));
    }

    public RootCaDetails getRootCaDetails() {
        return new RootCaDetails(getCaKeyOnHost(),getCaCertOnHost());
    }

}
