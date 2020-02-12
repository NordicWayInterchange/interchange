package no.vegvesen.ixn.federation.forwarding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DockerBaseIT {
	private static Logger logger = LoggerFactory.getLogger(DockerBaseIT.class);
	protected static final int HTTPS_PORT = 443;
	protected static final int AMQPS_PORT = 5671;
	protected static final int AMQP_PORT = 5672;
	private static final String CI_WORKDIR = "CIRCLE_WORKING_DIRECTORY";

	protected static Path getFolderPath(String dockerFolderName) {
		String projectFolder = "interchange";

		String ciWorkdir = System.getenv(CI_WORKDIR);
		if (ciWorkdir != null) {
			logger.debug("Circle CI because we got environment varaible [{}] with value [{}]", CI_WORKDIR, ciWorkdir);
			Path ciWorkdirPath = Paths.get(ciWorkdir);
			logger.debug("ci workdir environment variable [{}] with folder name [{}] reolves to full path [{}]", ciWorkdir, ciWorkdirPath.getFileName().toString(), ciWorkdirPath.toAbsolutePath());
			projectFolder = ciWorkdirPath.getFileName().toString();
		}

		Path run = Paths.get(".").toAbsolutePath();
		logger.debug("Resolving path to docker image folder [{}] from run path: [{}]", dockerFolderName, run.toAbsolutePath().toString());
		Path projectRoot = null;
		while (projectRoot == null && run.getParent() != null) {
			if (run.endsWith(projectFolder)) {
				projectRoot = run;
			}
			run = run.getParent();
		}
		if (projectRoot != null) {
			Path dockerFilePath = projectRoot.resolve(dockerFolderName);
			logger.debug("Resolved docker image folder [{}] to path [{}]", dockerFolderName, dockerFilePath.toAbsolutePath().toString());
			return dockerFilePath;
		}
		throw new RuntimeException("Could not resolve path to docker folder " + dockerFolderName + " in parent folder of " + run.toString());
	}

	protected static GenericContainer getQpidContainer(String configPathFromClasspath, String jksPathFromClasspath, final String caCertFile, final String serverCertFile, final String serverKeyFile) {
		return new GenericContainer(
				new ImageFromDockerfile().withFileFromPath(".", getFolderPath("qpid")))
				.withClasspathResourceMapping(configPathFromClasspath, "/config", BindMode.READ_ONLY)
				.withClasspathResourceMapping(jksPathFromClasspath, "/jks", BindMode.READ_ONLY)
				.withEnv("PASSWD_FILE", "/config/passwd")
				.withEnv("STATIC_GROUPS_FILE", "/config/groups")
				.withEnv("STATIC_VHOST_FILE", "/config/vhost.json")
				.withEnv("VHOST_FILE", "/work/default/config/default.json")
				.withEnv("GROUPS_FILE", "/work/default/config/groups")
				.withEnv("CA_CERTIFICATE_FILE", "/jks/" + caCertFile)
				.withEnv("SERVER_CERTIFICATE_FILE", "/jks/" + serverCertFile)
				.withEnv("SERVER_PRIVATE_KEY_FILE", "/jks/" + serverKeyFile)
				.withExposedPorts(AMQP_PORT, AMQPS_PORT, HTTPS_PORT, 8080);
	}

}
