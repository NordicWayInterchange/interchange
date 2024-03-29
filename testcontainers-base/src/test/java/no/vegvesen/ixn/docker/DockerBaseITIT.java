package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.QpidDockerBaseIT.KeysStructure;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DockerBaseITIT {

	static KeysStructure keysStructure = QpidDockerBaseIT.generateKeys(DockerBaseITIT.class,"my_ca","my-host");

	@Test
	void generateCaAndOneServerKeyWhichCanBeReadInTheDockerHostFileSystem(){
		assertThat(keysStructure.getKeysOutputPath()).isNotNull();
		assertThat(keysStructure.getKeysOutputPath()).exists();
		assertThat(keysStructure.getKeysOutputPath().resolve("my-host.p12")).exists();
	}
}