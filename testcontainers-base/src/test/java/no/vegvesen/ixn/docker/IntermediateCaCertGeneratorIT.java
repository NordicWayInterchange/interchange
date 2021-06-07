package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class IntermediateCaCertGeneratorIT {
    private Path resourcePath = Paths.get("src/test/resources");
    private Path crsPath = resourcePath.resolve("intermediateca").resolve("int.intermediate-test-domain.no.csr");
    private Path cakeysPath = resourcePath.resolve("rootca");
    private Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(IntermediateCaCertGeneratorIT.class);

    @Container
    private IntermediateCACertGenerator generator =new IntermediateCACertGenerator(
            DockerBaseIT.getFolderPath("keymaster").resolve("intermediateca/cert"),
            crsPath,
            "intermediate-test-domain.no",
            cakeysPath.resolve("ca.interchange-test-top-domain.eu.crt.pem"),
            cakeysPath.resolve("ca.interchange-test-top-domain.eu.key.pem"),
            containerOutPath
    );

    @Test
    public void checkInputPathExists() {
        assertThat(containerOutPath.resolve("chain.intermediate-test-domain.no.crt.pem")).exists();
    }

}
