package no.vegvesen.ixn.federation.forwarding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class QpidDockerBaseIT {
	private static Logger logger = LoggerFactory.getLogger(QpidDockerBaseIT.class);
	static final int HTTPS_PORT = 443;
	static final int AMQPS_PORT = 5671;
	private static final String CI_WORKDIR = "CIRCLE_WORKING_DIRECTORY";
	static final Path QPID_DOCKER_PATH = getQpidDockerPath();

	private static Path getQpidDockerPath() {
		String projectFolder = "interchange";

		String ciWorkdir = System.getenv(CI_WORKDIR);
		if (ciWorkdir != null) {
			Path ciWorkdirPath = Paths.get(ciWorkdir);
			logger.debug("ci workdir {} resolves to {} full path", ciWorkdir, ciWorkdirPath.getFileName().toString(), ciWorkdirPath.toAbsolutePath());
			projectFolder = ciWorkdirPath.getFileName().toString();
		}

		Path run = Paths.get(".").toAbsolutePath();
		logger.debug("Resolving qpid path from run path: " + run.toAbsolutePath().toString());
		Path projectRoot = null;
		while (projectRoot == null && run.getParent() != null) {
			if (run.endsWith(projectFolder)) {
				projectRoot = run;
			}
			run = run.getParent();
		}
		if (projectRoot != null) {
			Path qpid = projectRoot.resolve("qpid");
			logger.debug("Resolved qpid path {}", qpid.toAbsolutePath().toString());
			return qpid;
		}
		throw new RuntimeException("Could not resolve path to qpid docker folder in parent folder of " + run.toString());
	}
}
