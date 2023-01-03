package no.vegvesen.ixn.docker;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class RootCAKeyGenerator  extends GenericContainer<RootCAKeyGenerator> {


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
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
    }
}
