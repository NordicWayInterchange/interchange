package no.vegvesen.ixn.docker;

/*-
 * #%L
 * testcontainers-base
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DockerBaseIT {
	private static Logger logger = LoggerFactory.getLogger(DockerBaseIT.class);
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

}
