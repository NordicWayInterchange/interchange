package no.vegvesen.ixn.federation.forwarding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class QpidDockerBaseIT {
	private static Logger logger = LoggerFactory.getLogger(QpidDockerBaseIT.class);
	static final int HTTPS_PORT = 443;
	static final int AMQPS_PORT = 5671;
	private static final String CI_WORKDIR = "CIRCLE_WORKING_DIRECTORY";
	private static final Path QPID_DOCKER_PATH = getQpidDockerPath();

	private static Path getQpidDockerPath() {
		String projectFolder = "interchange";

		String ciWorkdir = System.getenv(CI_WORKDIR);
		if (ciWorkdir != null) {
			logger.debug("Circle CI because we got environment varaible [{}] with value [{}]", CI_WORKDIR, ciWorkdir);
			Path ciWorkdirPath = Paths.get(ciWorkdir);
			logger.debug("ci workdir environment variable [{}] with folder name [{}] reolves to full path [{}]", ciWorkdir, ciWorkdirPath.getFileName().toString(), ciWorkdirPath.toAbsolutePath());
			projectFolder = ciWorkdirPath.getFileName().toString();
		}

		Path run = Paths.get(".").toAbsolutePath();
		logger.debug("Resolving qpid path from run path: [{}]", run.toAbsolutePath().toString());
		Path projectRoot = null;
		while (projectRoot == null && run.getParent() != null) {
			if (run.endsWith(projectFolder)) {
				projectRoot = run;
			}
			run = run.getParent();
		}
		if (projectRoot != null) {
			Path qpid = projectRoot.resolve("qpid");
			logger.debug("Resolved qpid path [{}]", qpid.toAbsolutePath().toString());
			return qpid;
		}
		throw new RuntimeException("Could not resolve path to qpid docker folder in parent folder of " + run.toString());
	}

	protected GenericContainer getQpidContainer(String configPathFromClasspath, String jksPathFromClasspath) {
		return new GenericContainer(
				new ImageFromDockerfile().withFileFromPath(".", QPID_DOCKER_PATH))
				.withClasspathResourceMapping(configPathFromClasspath, "/config", BindMode.READ_ONLY)
				.withClasspathResourceMapping(jksPathFromClasspath, "/jks", BindMode.READ_ONLY)
				.withEnv("PASSWD_FILE", "/config/passwd")
				.withEnv("STATIC_GROUPS_FILE", "/config/groups")
				.withEnv("STATIC_VHOST_FILE", "/config/vhost.json")
				.withEnv("VHOST_FILE", "/work/default/config/default.json")
				.withEnv("GROUPS_FILE", "/work/default/config/groups")
				.withEnv("CA_CERTIFICATE_FILE", "/jks/my_ca.crt")
				.withEnv("SERVER_CERTIFICATE_FILE", "/jks/localhost.crt")
				.withEnv("SERVER_PRIVATE_KEY_FILE", "/jks/localhost.key")
				.withExposedPorts(AMQPS_PORT, HTTPS_PORT);
	}
}
