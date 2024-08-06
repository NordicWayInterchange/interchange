package no.vegvesen.ixn.docker.keygen.generator;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.time.Duration;

public class RootCAKeyGenerator  extends GenericContainer<RootCAKeyGenerator> {


    private Path keysFolder;
    private String caDomain;
    private String countryCode;

    public RootCAKeyGenerator(ImageFromDockerfile image, Path keysFolder, String caDomain, String countryCode) {
        super(image);
        this.keysFolder = keysFolder;
        this.caDomain = caDomain;
        this.countryCode = countryCode;
    }

    public RootCAKeyGenerator(Path keysFolder, String caDomain, String countryCode) {
        this(new ImageFromDockerfile("vinz-clortho")
                .withFileFromClasspath(".", "rootca/newca"),
                keysFolder,caDomain,countryCode);

    }

    @Override
    protected void configure() {
        this.copyFileToContainer(MountableFile.forHostPath(keysFolder), "/ca_keys");
        this.withCommand(caDomain,countryCode);
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


    public CertKeyPair getCertKeyPairOnHost() {
        return new CertKeyPair(getCaCertOnHost(),getCaKeyOnHost());
    }

}
