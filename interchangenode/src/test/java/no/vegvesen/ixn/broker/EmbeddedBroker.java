package no.vegvesen.ixn.broker;

import org.apache.qpid.server.SystemLauncher;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedBroker
{
	private static final String INITIAL_CONFIGURATION = "qpid-embedded/embed-initial-config-no.json";
	private SystemLauncher systemLauncher;

	public static void main(String args[]) {
		EmbeddedBroker broker = new EmbeddedBroker();
		try {
			broker.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
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
		URL initialConfig = EmbeddedBroker.class.getClassLoader().getResource(INITIAL_CONFIGURATION);
		attributes.put("type", "Memory");
		attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
		attributes.put("startupLoggedToSystemOut", true);
		return attributes;
	}
}