package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class ServiceProviderCSRGeneratorIT {
    private static Path targetPath = DockerBaseIT.getTargetFolderPathForTestClass(ServiceProviderCSRGeneratorIT.class);

    @Container
    private static ServiceProviderCSRGenerator generator = new ServiceProviderCSRGenerator(
            DockerBaseIT.getFolderPath("keymaster").resolve("serviceprovider/csr"),
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
