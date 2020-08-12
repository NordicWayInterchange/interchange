package no.vegvesen.ixn.docker;

import net.jcip.annotations.NotThreadSafe;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@NotThreadSafe
public class QpidDockerBaseIT extends DockerBaseIT {
	public static final int HTTPS_PORT = 443;
	public static final int AMQPS_PORT = 5671;
	public static final int AMQP_PORT = 5672;

	@SuppressWarnings("rawtypes")
	public static GenericContainer getQpidContainer(String configPathFromClasspath, String jksPathFromClasspath, final String keyStore, final String keyStorePassword, final String trustStore, String trustStorePassword) {
		return new GenericContainer(
				new ImageFromDockerfile("qpid-it", false).withFileFromPath(".", getFolderPath("qpid")))
				.withClasspathResourceMapping(configPathFromClasspath, "/config", BindMode.READ_ONLY)
				.withClasspathResourceMapping(jksPathFromClasspath, "/jks", BindMode.READ_ONLY)
				.withEnv("PASSWD_FILE", "/config/passwd")
				.withEnv("STATIC_GROUPS_FILE", "/config/groups")
				.withEnv("STATIC_VHOST_FILE", "/config/vhost.json")
				.withEnv("VHOST_FILE", "/work/default/config/default.json")
				.withEnv("GROUPS_FILE", "/work/default/config/groups")
				.withEnv("KEY_STORE", "/jks/" + keyStore)
				.withEnv("KEY_STORE_PASSWORD", keyStorePassword)
				.withEnv("TRUST_STORE", "/jks/" + trustStore)
				.withEnv("TRUST_STORE_PASSWORD", trustStorePassword)
				.withExposedPorts(AMQP_PORT, AMQPS_PORT, HTTPS_PORT, 8080);
	}
}