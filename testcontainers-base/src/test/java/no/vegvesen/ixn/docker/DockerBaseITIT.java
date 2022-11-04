package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class DockerBaseITIT {

	private static Logger logger = LoggerFactory.getLogger(DockerBaseITIT.class);

	@Container
	static KeysContainer keysContainer = DockerBaseIT.getKeyContainer(DockerBaseITIT.class,"my_ca","my-host")
			.withLogConsumer(new Slf4jLogConsumer(logger));

	@Test
	void generateCaAndOneServerKeyWhichCanBeReadInTheDockerHostFileSystem(){
		assertThat(keysContainer.getKeyFolderOnHost()).isNotNull();
		assertThat(keysContainer.getKeyFolderOnHost()).exists();
		assertThat(keysContainer.getKeyFolderOnHost().resolve("my-host.p12")).exists();
	}
}