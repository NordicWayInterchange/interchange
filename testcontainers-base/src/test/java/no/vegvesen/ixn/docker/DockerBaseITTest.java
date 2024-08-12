package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DockerBaseITTest {

	@Test
	void testcontainersBaseFolderCanBeResolved() {
		Path folderPath = DockerBaseIT.getFolderPath("testcontainers-base");
		assertThat(folderPath).isNotNull();
		assertThat(folderPath.toString()).endsWith("testcontainers-base");
	}

	@Test
	void getFolderPathOfNonExistingFolderWillGivePath() {
		Path folderPath = DockerBaseIT.getFolderPath("this-folder-does-not-exist");
		assertThat(folderPath).isNotNull();
		File f = new File(folderPath.toUri());
		assertThat(f.exists()).isFalse();
	}


}
