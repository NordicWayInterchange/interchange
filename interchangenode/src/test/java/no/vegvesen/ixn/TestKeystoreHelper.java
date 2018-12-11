package no.vegvesen.ixn;

import java.net.URL;

public class TestKeystoreHelper {

	public static final String JAVAX_NET_SSL_KEY_STORE = "javax.net.ssl.keyStore";
	public static final String JAVAX_NET_SSL_TRUST_STORE = "javax.net.ssl.trustStore";

	public static void useTestKeystore(String userKeystore, String truststore) {
		if (userKeystore != null) {
			System.setProperty(JAVAX_NET_SSL_KEY_STORE, getFilePath(userKeystore));
			System.setProperty("javax.net.ssl.keyStorePassword", "password");
			System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
		}
		if (truststore != null) {
			System.setProperty(JAVAX_NET_SSL_TRUST_STORE, getFilePath(truststore));
			System.setProperty("javax.net.ssl.trustStorePassword", "password");
		}
	}

	private static String getFilePath(String jksTestResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load test jks resource " + jksTestResource);
	}
}
