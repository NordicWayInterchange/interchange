package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class QpidDockerBaseITIT {
	static Path testKeysFolder = DockerBaseIT.getFolderPath("target/test-keys");

	@SuppressWarnings("rawtypes")
	@Container
	public static GenericContainer keyContainer = QpidDockerBaseIT.getKeyContainer(testKeysFolder,"my_ca", "localhost");

	@Test
	void generateCaAndOneServerKeyWhichCanBeReadInTheDockerHostFileSystem(){
		assertThat(keyContainer).isNotNull();
		assertThat(testKeysFolder.toFile()).exists();
		assertThat(new File(testKeysFolder.toAbsolutePath().toString() + "/localhost.p12")).exists();
	}
}