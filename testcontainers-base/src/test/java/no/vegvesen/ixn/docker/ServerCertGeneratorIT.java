package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class ServerCertGeneratorIT {

    private static Path resourcePath = Paths.get("src/test/resources");
    private static Path intermediateCaKeysPath = resourcePath.resolve("intermediateca");
    private static Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(ServerCertGeneratorIT.class);

    @Container
    private static ServerCertGenerator generator = new ServerCertGenerator(
            DockerBaseIT.getFolderPath("keymaster").resolve("server"),
            "testserver.test2.no",
            intermediateCaKeysPath.resolve("int.test2.no.crt.pem"),
            intermediateCaKeysPath.resolve("int.test2.no.key.pem"),
            intermediateCaKeysPath.resolve("chain.test2.no.crt.pem"),
            "NO",
            containerOutPath
    );

    @Test
    public void checkContainerOutput() {
        Path chain = containerOutPath.resolve("chain.testserver.test2.no.crt.pem");
        assertThat(chain).exists();
    }



}
