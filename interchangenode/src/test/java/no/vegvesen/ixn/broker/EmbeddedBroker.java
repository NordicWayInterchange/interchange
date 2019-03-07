package no.vegvesen.ixn.broker;

import org.apache.qpid.server.SystemLauncher;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedBroker
{
	private final String initialConfigurationFile;
	private SystemLauncher systemLauncher;

	public EmbeddedBroker(String initialConfigurationFile) {
		this.initialConfigurationFile = initialConfigurationFile;
	}

	public void start() throws Exception {
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
}