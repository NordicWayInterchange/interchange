package no.vegvesen.ixn.docker;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class IntermediateCACertGenerator extends GenericContainer<IntermediateCACertGenerator> {

    private final String csrPathInContainer;
    private String domainName;
    private String csrPathOnHost;

    public IntermediateCACertGenerator(Path dockerFilePath, Path inputCsr, String domainName) {
        super(new ImageFromDockerfile("intermediate-ca-cert-generator")
                .withFileFromPath(".",dockerFilePath));

        this.domainName = domainName;
        csrPathOnHost = inputCsr.toString();
        csrPathInContainer = "/csr_in/" + inputCsr.getFileName().toString();
    }

    /**
     * Maps the csr as a file to the /csr_in/ folder, keeping the file name.
     */
    @Override
    protected void configure() {
        this.withFileSystemBind(csrPathOnHost,
                csrPathInContainer,
                BindMode.READ_ONLY);
        this.withCommand(csrPathInContainer,domainName);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(5)));
    }
}
