package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class IntermediateCaCertGeneratorIT {
    private Path cakeysPath = Paths.get("src/test/resources/rootca");
    private Path containerOutPath = DockerBaseIT.getTargetFolderPathForTestClass(IntermediateCaCertGeneratorIT.class);


    @Test
    public void checkInputPathExists() {
        assertThat(cakeysPath).exists();

    }

}
