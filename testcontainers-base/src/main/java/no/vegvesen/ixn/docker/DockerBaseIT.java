package no.vegvesen.ixn.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DockerBaseIT {
	private static Logger logger = LoggerFactory.getLogger(DockerBaseIT.class);
	private static final String CI_WORKDIR = "CIRCLE_WORKING_DIRECTORY";

	protected static Path getFolderPath(String relativePathFromProjectRoot) {
		Path projectRoot = getProjectRootPath();
		logger.debug("Resolving path to project folder [{}] from project root path: [{}]", relativePathFromProjectRoot, projectRoot.toString());
		Path dockerFilePath = projectRoot.resolve(relativePathFromProjectRoot);
		logger.debug("Resolved path to project folder [{}] to path [{}]", relativePathFromProjectRoot, dockerFilePath.toAbsolutePath().toString());
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
			throw new RuntimeException("Could not resolve path to project root in parent folder of " + run.toString());
		}
		return projectRoot;
	}

	public static Path getTargetFolderPathForTestClass(Class clazz) {
		return getFolderPath("target/test-keys-" + clazz.getSimpleName());
	}

}
