package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class DockerBaseITIT {
	static Path testKeysFolder = DockerBaseIT.generateKeys(DockerBaseITIT.class, "my_ca", "localhost");

	@Test
	void generateCaAndOneServerKeyWhichCanBeReadInTheDockerHostFileSystem(){
		assertThat(testKeysFolder).isNotNull();
		assertThat(testKeysFolder.toFile()).exists();
		assertThat(new File(testKeysFolder.toAbsolutePath().toString() + "/localhost.p12")).exists();
	}
}