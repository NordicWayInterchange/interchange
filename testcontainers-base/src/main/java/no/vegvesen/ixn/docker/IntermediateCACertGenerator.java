package no.vegvesen.ixn.docker;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class IntermediateCACertGenerator extends GenericContainer<IntermediateCACertGenerator> {

    public static final String CA_IN_FOLDER = "/ca_in/";
    public static final String CSR_IN_FOLDER = "/csr_in/";
    public static final String KEYS_OUT_FOLDER = "/keys_out/";
    private final String csrPathInContainer;
    private Path caCert;
    private Path caKey;
    private Path targetPath;
    private String countryCode;
    private String domainName;
    private String csrPathOnHost;

    public IntermediateCACertGenerator(Path dockerFilePath,
                                       Path inputCsr,
                                       String domainName,
                                       Path caCert,
                                       Path caKey,
                                       String countryCode,
                                       Path targetPath) {
        super(new ImageFromDockerfile("intermediate-ca-cert-generator")
                .withFileFromPath(".",dockerFilePath));

        this.domainName = domainName;
        csrPathOnHost = inputCsr.toString();
        csrPathInContainer = CSR_IN_FOLDER + inputCsr.getFileName().toString();
        this.caCert = caCert;
        this.caKey = caKey;
        this.targetPath = targetPath;
        this.countryCode = countryCode;
    }

    /**
     * Maps the csr as a file to the /csr_in/ folder, keeping the file name.
     * TODO use Path class to map from host to container path without using + on strings.
     */
    @Override
    protected void configure() {
        String caCertPathInContainer = CA_IN_FOLDER + caCert.getFileName().toString();
        String caKeyPathInContainer = CA_IN_FOLDER + caKey.getFileName().toString();
        this.withFileSystemBind(csrPathOnHost,
                csrPathInContainer,
                BindMode.READ_ONLY);
        this.withFileSystemBind(caCert.toString(), caCertPathInContainer,
                BindMode.READ_ONLY);
        this.withFileSystemBind(caKey.toString(), caKeyPathInContainer,
                BindMode.READ_ONLY);
        this.withFileSystemBind(targetPath.toString(), KEYS_OUT_FOLDER,BindMode.READ_WRITE);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
        this.withCommand(csrPathInContainer,domainName,caCertPathInContainer,caKeyPathInContainer,countryCode);
    }


    public Path getSingleCertOnHost() {
        return targetPath.resolve(String.format("int.%s.crt.pem",domainName));
    }

    public Path getChainCertOnHost() {
        return targetPath.resolve(String.format("chain.%s.crt.pem",domainName));
    }
}
