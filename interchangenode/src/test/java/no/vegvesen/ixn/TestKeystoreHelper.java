package no.vegvesen.ixn;

import java.net.URL;

public class TestKeystoreHelper {

	private static final String GUEST_KEYSTORE = "jks/guest.p12";
	private static final String TRUSTSTORE = "jks/truststore.jks";

	public static void useTestKeystore() {
		System.setProperty("javax.net.ssl.keyStore", getFilePath(GUEST_KEYSTORE));
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
		System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");

		System.setProperty("javax.net.ssl.trustStore", getFilePath(TRUSTSTORE));
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
