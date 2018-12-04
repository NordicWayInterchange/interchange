package no.vegvesen.ixn;

import java.net.URL;

public class TestKeystoreHelper {

	public static void useTestKeystore(String userKeystore, String truststore) {
		System.setProperty("javax.net.ssl.keyStore", getFilePath(userKeystore));
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
		System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");

		System.setProperty("javax.net.ssl.trustStore", getFilePath(truststore));
		System.setProperty("javax.net.ssl.trustStorePassword", "password");
	}

	private static String getFilePath(String jksTestResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load test jks resource " + jksTestResource);
	}
}
