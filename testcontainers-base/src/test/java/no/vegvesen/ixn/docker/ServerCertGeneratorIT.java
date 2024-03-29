package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.generator.ServerCertGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@Disabled
public class ServerCertGeneratorIT {

    private static Path resourcePath = Paths.get("src/test/resources");
    private static Path intermediateCaKeysPath = resourcePath.resolve("intermediateca");
    private static Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(ServerCertGeneratorIT.class);

    @Container
    private static ServerCertGenerator generator = new ServerCertGenerator(
            "testserver.test2.no",
            intermediateCaKeysPath.resolve("int.test2.no.crt.pem"),
            intermediateCaKeysPath.resolve("int.test2.no.key.pem"),
            intermediateCaKeysPath.resolve("chain.test2.no.crt.pem"),
            "NO",
            containerOutPath
    );

    @Test
    public void checkContainerOutput() {
        Path chain = generator.getCertOnHost();
        assertThat(chain).exists();
    }



}
