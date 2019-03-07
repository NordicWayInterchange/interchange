package no.vegvesen.ixn.broker;

import org.apache.qpid.server.SystemLauncher;
import org.springframework.util.SocketUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedBroker
{
	private final String initialConfigurationFile;
	private final int port;
	private SystemLauncher systemLauncher;

	public EmbeddedBroker(String initialConfigurationFile) throws Exception {
		this.initialConfigurationFile = initialConfigurationFile;
		port = SocketUtils.findAvailableTcpPort();
		this.start();
	}

	private void start() throws Exception {
		System.setProperty("qpid.amqp_port", "" + port);
		systemLauncher = new SystemLauncher();
		systemLauncher.startup(createSystemConfig());
	}

	public void stop() {
		systemLauncher.shutdown();
	}

	private Map<String, Object> createSystemConfig() {
		Map<String, Object> attributes = new HashMap<>();
		URL initialConfig = EmbeddedBroker.class.getClassLoader().getResource(initialConfigurationFile);
		attributes.put("type", "Memory");
		attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
		attributes.put("startupLoggedToSystemOut", true);
		return attributes;
	}

	public String getURI() {
		return "amqp://localhost:" + port;
	}
}