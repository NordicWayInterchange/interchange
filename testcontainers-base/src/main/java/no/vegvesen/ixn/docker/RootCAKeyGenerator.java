package no.vegvesen.ixn.docker;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;

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
        //this.withStartupCheckStrategy(new IndefiniteWaitOneShotStartupCheckStrategy());
        this.waitingFor(Wait.forLogMessage(".*CA Key generation done.*\\n",1));

    }
}
