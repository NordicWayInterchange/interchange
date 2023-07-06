package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.docker.DockerBaseIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.time.Duration;

public class ServiceProviderCertGenerator extends GenericContainer<ServiceProviderCertGenerator> {

    private static String INTERMEDIATE_CA_IN_FOLDER = "/int_ca_in/";
    private static String CSR_IN_PATH = "/csr_in/";
    private static String CERT_OUT_FOLDER = "/cert_out/";
    private final String hostCsrPath;
    private final String containerCsrPath;
    private final String hostCaCertPath;
    private final String containerCaCertPath;
    private final String hostCaKeyPath;
    private final String containerCaKeyPath;
    private final String containerCertChainPath;
    private Path intermediateCaCertChainPath;
    private final String hostOutputPath;
    private final Path outputPath;
    private final String containerOutputPath;
    private String clientName;


    public ServiceProviderCertGenerator(ImageFromDockerfile image,
                                        Path inputCsr,
                                        String clientName,
                                        Path intermediateCacertPath,
                                        Path intermediateCaKeyPath,
                                        Path intermediateCaCertChainPath,
                                        Path outputPath) {
        super(image);
        this.hostCsrPath = inputCsr.toString();
        this.containerCsrPath = CSR_IN_PATH +inputCsr.getFileName().toString();
        this.clientName = clientName;
        this.hostCaCertPath = intermediateCacertPath.toString();
        this.containerCaCertPath = INTERMEDIATE_CA_IN_FOLDER + intermediateCacertPath.getFileName().toString();
        this.hostCaKeyPath = intermediateCaKeyPath.toString();
        this.containerCaKeyPath = INTERMEDIATE_CA_IN_FOLDER + intermediateCaKeyPath.getFileName().toString();
        this.intermediateCaCertChainPath = intermediateCaCertChainPath;
        this.hostOutputPath = outputPath.toString();
        this.outputPath = outputPath;
        this.containerOutputPath = CERT_OUT_FOLDER;
        this.containerCertChainPath = INTERMEDIATE_CA_IN_FOLDER + intermediateCaCertChainPath.getFileName().toString();

    }

    public ServiceProviderCertGenerator(Path inputCsr,
                                        String clientName,
                                        Path intermediateCacertPath,
                                        Path intermediateCaKeyPath,
                                        Path intermediateCaCertChainPath,
                                        Path outputPath) {

        this(new ImageFromDockerfile("sp-cert-generator").withFileFromClasspath(".", "intermediateca/sign_sp"),
                inputCsr,
                clientName,
                intermediateCacertPath,
                intermediateCaKeyPath,
                intermediateCaCertChainPath,
                outputPath);
    }

        @Override
    public void configure() {
        this.withFileSystemBind(hostCsrPath,containerCsrPath, BindMode.READ_ONLY);
        this.withFileSystemBind(hostCaCertPath,containerCaCertPath,BindMode.READ_ONLY);
        this.withFileSystemBind(hostCaKeyPath,containerCaKeyPath,BindMode.READ_ONLY);
        this.withFileSystemBind(intermediateCaCertChainPath.toString(),containerCertChainPath,BindMode.READ_ONLY);
        this.withFileSystemBind(hostOutputPath,containerOutputPath,BindMode.READ_WRITE);
        this.withCommand(containerCsrPath,clientName,containerCaCertPath,containerCaKeyPath,containerCertChainPath);
        this.withStartupCheckStrategy(new OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)));

    }

    public Path getCertOnHost() {
        return outputPath.resolve(String.format("%s.crt.pem",clientName));
    }

    public Path getCertChainOnHost() {
        return outputPath.resolve(String.format("chain.%s.crt.pem",clientName));
    }

    public CertAndCertChain getCertAndCertChainOnHost() {
        return new CertAndCertChain(getCertOnHost(),getCertChainOnHost());

    }
}
