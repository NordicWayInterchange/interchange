package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.generator.RootCAKeyGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@Disabled
public class RootCAKeyGeneratorIT {

    private static Path targetPath = DockerBaseIT.getTargetFolderPathForTestClass(RootCAKeyGenerator.class);

    @Container
    private RootCAKeyGenerator keysGenerator = new RootCAKeyGenerator(
            targetPath,
            "test.no",
            "NO");

    @Test
    public void testGenerateKeys() throws IOException {
        Path certPath = targetPath.resolve("ca.test.no.crt.pem");
        assertThat(certPath).exists();
        assertThat(Files.size(certPath)).isGreaterThan(0);
        assertThat(targetPath.resolve("ca.test.no.key.pem")).exists();
    }


}
