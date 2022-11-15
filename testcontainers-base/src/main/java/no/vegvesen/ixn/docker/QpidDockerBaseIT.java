package no.vegvesen.ixn.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class QpidDockerBaseIT extends DockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(QpidDockerBaseIT.class);

	public static QpidContainer getQpidTestContainer(String configPathFromClasspath, Path testKeysPath, final String keyStore, final String keyStorePassword, final String trustStore, String trustStorePassword, String vHostName) {
		Path imageLocation = getFolderPath("qpid-test");
		Path configPath = Paths.get(configPathFromClasspath);
		logger.debug("Creating container qpid-it-memory, from Docker file from {} and config from {}",
				imageLocation,configPath);
		return new QpidContainer("qpid-it-memory",
				imageLocation,
				configPath,
				testKeysPath,
				keyStore,
				keyStorePassword,
				trustStore,
				trustStorePassword,
				vHostName);
	}
}