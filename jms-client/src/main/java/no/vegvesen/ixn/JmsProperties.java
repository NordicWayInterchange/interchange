package no.vegvesen.ixn;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JmsProperties {
	static Properties getProperties(String[] args, String propertiesFile) throws IOException {
		InputStream in;
		if (args.length == 1) {
			in = new FileInputStream(args[0]);
		} else {
			in = Source.class.getResourceAsStream(propertiesFile);
		}
		Properties props = new Properties();
		props.load(in);
		return props;
	}
}
