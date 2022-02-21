package no.vegvesen.ixn.docker;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class ServerCertGenerator extends GenericContainer<ServerCertGenerator> {

    private static final String CA_IN_FOLDER = "/ca_in/";
    private static final String KEYS_OUT_FOLDER = "/keys_out/";
    private final String caCertOnHost;
    private final String caCertInContainer;
    private final String caKeyOnHost;
    private final String caKeyInContainer;
    private final String chainOnHost;
    private final String chainInContainer;
    private String countryCode;
    private Path targetPath;

    private String domainName;

    public ServerCertGenerator(Path dockerFilePath,
                               String domainName,
                               Path intermediateCACert,
                               Path intermediateCAKey,
                               Path intermedateCAChain,
                               String countryCode,
                               Path targetPath) {
        super(new ImageFromDockerfile("server-cert-generator")
                .withFileFromPath(".",dockerFilePath));
        this.domainName = domainName;
        caCertOnHost =  intermediateCACert.toString();
        caCertInContainer = CA_IN_FOLDER + intermediateCACert.getFileName().toString();
        caKeyOnHost = intermediateCAKey.toString();
        caKeyInContainer = CA_IN_FOLDER + intermediateCAKey.getFileName().toString();
        chainOnHost = intermedateCAChain.toString();
        chainInContainer = CA_IN_FOLDER + intermedateCAChain.getFileName().toString();
        this.countryCode = countryCode;
        this.targetPath = targetPath;
    }


    @Override
    public void configure() {

        this.withFileSystemBind(caCertOnHost,caCertInContainer, BindMode.READ_ONLY);
        this.withFileSystemBind(caKeyOnHost,caKeyInContainer,BindMode.READ_ONLY);
        this.withFileSystemBind(chainOnHost,chainInContainer,BindMode.READ_ONLY);
        this.withFileSystemBind(targetPath.toString(),KEYS_OUT_FOLDER,BindMode.READ_WRITE);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));
        this.withCommand(domainName,caKeyInContainer,caCertInContainer,chainInContainer,countryCode);
    }

    public Path getKeyOnHost() {
        return targetPath.resolve(String.format("%s.key.pem",domainName));
    }

    public Path getCertOnHost() {
        return targetPath.resolve(String.format("%s.crt.pem",domainName));
    }

    public Path getCertChainOnHost() {
        return targetPath.resolve(String.format("chain.%s.crt.pem",domainName));
    }

    public ServerCertDetails getServiceCertDetails() {
        return new ServerCertDetails(getKeyOnHost(),getCertOnHost(),getCertChainOnHost());
    }
}
