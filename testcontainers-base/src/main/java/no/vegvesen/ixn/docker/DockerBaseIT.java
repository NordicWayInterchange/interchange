package no.vegvesen.ixn.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DockerBaseIT {
	private static final Logger logger = LoggerFactory.getLogger(DockerBaseIT.class);
	private static final String CI_WORKDIR = "CIRCLE_WORKING_DIRECTORY";

	public static String getDockerHost() {
		return DockerClientFactory.instance().dockerHostIpAddress();
	}

	protected static Path getFolderPath(String relativePathFromProjectRoot) {
		Path projectRoot = getProjectRootPath();
		logger.debug("Resolving path to project folder [{}] from project root path: [{}]", relativePathFromProjectRoot, projectRoot);
		Path dockerFilePath = projectRoot.resolve(relativePathFromProjectRoot);
		logger.debug("Resolved path to project folder [{}] to path [{}]", relativePathFromProjectRoot, dockerFilePath.toAbsolutePath());
		return dockerFilePath;
	}

	private static Path getProjectRootPath() {
		String projectFolder = "interchange";

		String ciWorkdir = System.getenv(CI_WORKDIR);
		if (ciWorkdir != null) {
			logger.debug("Circle CI because we got environment varaible [{}] with value [{}]", CI_WORKDIR, ciWorkdir);
			Path ciWorkdirPath = Paths.get(ciWorkdir);
			logger.debug("ci workdir environment variable [{}] with folder name [{}] reolves to full path [{}]", ciWorkdir, ciWorkdirPath.getFileName().toString(), ciWorkdirPath.toAbsolutePath());
			projectFolder = ciWorkdirPath.getFileName().toString();
		}

		Path run = Paths.get(".").toAbsolutePath();
		Path projectRoot = null;
		while (projectRoot == null && run.getParent() != null) {
			if (run.endsWith(projectFolder)) {
				projectRoot = run;
			}
			run = run.getParent();
		}
		if (projectRoot == null ) {
			throw new RuntimeException("Could not resolve path to project root in parent folder of " + run);
		}
		return projectRoot;
	}

	public static Path getTargetFolderPathForTestClass(Class<?> clazz) {
		return getFolderPath("target/test-keys-" + clazz.getSimpleName());
	}

}
