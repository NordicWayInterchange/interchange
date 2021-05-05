package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class RootCAKeyGeneratorIT {

    private static Path targetPath = DockerBaseIT.getTargetFolderPathForTestClass(RootCAKeyGenerator.class);

    @Container
    private RootCAKeyGenerator keysGenerator = new RootCAKeyGenerator(
            DockerBaseIT.getFolderPath("keymaster").resolve("rootca"),
            targetPath,
            "test.no",
            "NO");

    @Test
    public void testGenerateKeys() {
        assertThat(targetPath.resolve("ca.test.no.crt.pem")).exists();
        assertThat(targetPath.resolve("ca.test.no.key.pem")).exists();

    }


}
