package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.generator.ServiceProviderCertGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@Disabled
public class ServiceProviderCertGeneratorIT {

    static Path resourcePath = Paths.get("src/test/resources");
    static Path intermediateCaPath = resourcePath.resolve("intermediateca");
    static Path testPrefixedOutputPath = DockerBaseIT.getTargetFolderPathForTestClass(ServiceProviderCertGeneratorIT.class);

    @Container
    static ServiceProviderCertGenerator generator = new ServiceProviderCertGenerator(
            resourcePath.resolve("serviceprovider/testprovider.csr.pem"),
            "testprovider",
            intermediateCaPath.resolve("int.test2.no.crt.pem"),
            intermediateCaPath.resolve("int.test2.no.key.pem"),
            intermediateCaPath.resolve("chain.test2.no.crt.pem"),
            testPrefixedOutputPath
    );


    @Test
    public void testCertificateGenerated() {
        assertThat(testPrefixedOutputPath.resolve("chain.testprovider.crt.pem")).exists();
        assertThat(testPrefixedOutputPath.resolve("testprovider.crt.pem")).exists();

    }

}
