package no.vegvesen.ixn.docker;


import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class IntermediateCaCSRGeneratorIT {
    private static Path targetPath = DockerBaseIT.getTargetFolderPathForTestClass(IntermediateCaCSRGeneratorIT.class);

    @Container
    private IntermediateCaCSRGenerator generator = new IntermediateCaCSRGenerator(
            DockerBaseIT.getFolderPath("keymaster").resolve("intermediateca/csr"),
            targetPath,
            "test2.no",
            "NO");

    @Test
    public void testGenerateKeyAndCSR() {
       assertThat(targetPath.resolve("int.test2.no.key.pem")) .exists();
       assertThat(targetPath.resolve("int.test2.no.csr")).exists();
    }
}
