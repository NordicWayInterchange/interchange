package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.generator.IntermediateCACertGenerator;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class IntermediateCaCertGeneratorIT {


    private static Path resourcePath = Paths.get("src/test/resources");
    private static Path crsPath = resourcePath.resolve("intermediateca/int.test2.no.csr");
    private static Path cakeysPath = resourcePath.resolve("rootca");
    private static Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(IntermediateCaCertGeneratorIT.class);

    private static String countryCode = "NO";
    @Container
    private static IntermediateCACertGenerator generator =new IntermediateCACertGenerator(
            crsPath,
            "test2.no",
            cakeysPath.resolve("ca.test.no.crt.pem"),
            cakeysPath.resolve("ca.test.no.key.pem"),
            countryCode,
            containerOutPath
    );


    @Test
    public void checkInputPathExists() throws IOException {
        Path certificateChain = containerOutPath.resolve("chain.test2.no.crt.pem");
        assertThat(certificateChain).exists();
        assertThat(Files.size(certificateChain)).isGreaterThan(0l);
        assertThat(containerOutPath.resolve("int.test2.no.crt.pem")).exists();
    }

}
