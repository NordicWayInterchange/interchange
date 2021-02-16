package no.vegvesen.ixn.docker;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DockerBaseITTest {

	@Test
	void testcontainersBaseFolderCanBeResolved() {
		Path folderPath = DockerBaseIT.getProjectRelativePath("testcontainers-base");
		assertThat(folderPath).isNotNull();
		assertThat(folderPath.toString()).endsWith("testcontainers-base");
	}

	@Test
	void keyGenDockerImageFolderCanBeResolved() {
		Path folderPath = DockerBaseIT.getProjectRelativePath("key-gen");
		assertThat(folderPath).isNotNull();
		assertThat(folderPath.toString()).endsWith("key-gen");
		File f = new File(folderPath.toUri());
		assertThat(f.exists()).isTrue();
	}

	@Test
	void getFolderPathOfNonExistingFolderWillGivePath() {
		Path folderPath = DockerBaseIT.getProjectRelativePath("this-folder-does-not-exist");
		assertThat(folderPath).isNotNull();
		File f = new File(folderPath.toUri());
		assertThat(f.exists()).isFalse();
	}


}
