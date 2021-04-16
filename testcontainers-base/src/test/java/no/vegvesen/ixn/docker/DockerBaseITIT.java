package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class DockerBaseITIT {

	@Container
	static KeysContainer keysContainer = DockerBaseIT.getKeyContainer(DockerBaseITIT.class,"my_ca","my-host");

	@Test
	void generateCaAndOneServerKeyWhichCanBeReadInTheDockerHostFileSystem(){
		assertThat(keysContainer.getKeyFolderOnHost()).isNotNull();
		assertThat(keysContainer.getKeyFolderOnHost()).exists();
		assertThat(keysContainer.getKeyFolderOnHost().resolve("my-host.p12")).exists();
	}
}