package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.generator.ServiceProviderCSRGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@Disabled
public class ServiceProviderCSRGeneratorIT {
    private static Path targetPath = DockerBaseIT.getTargetFolderPathForTestClass(ServiceProviderCSRGeneratorIT.class);

    @Container
    private static ServiceProviderCSRGenerator generator = new ServiceProviderCSRGenerator(
            targetPath,
            "testprovider",
            "NO"
    );

    @Test
    public void testGenerateServiceProviderCSR() {
        assertThat(targetPath.resolve("testprovider.csr.pem")).exists();
        assertThat(targetPath.resolve("testprovider.key.pem")).exists();
    }
}
