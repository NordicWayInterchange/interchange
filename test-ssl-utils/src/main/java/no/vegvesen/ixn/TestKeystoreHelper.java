package no.vegvesen.ixn;

/*-
 * #%L
 * test-ssl-utils
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

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.net.ssl.SSLContext;
import java.net.URL;

public class TestKeystoreHelper {

	private static final String TEST_KEYSTORE_PASSWORD = "password";
	private static final KeystoreType TRUSTSTORE_TYPE = KeystoreType.JKS;
	private static final KeystoreType KEYSTORE_TYPE = KeystoreType.PKCS12;


	public static SSLContext sslContext(String keystore, String truststore) throws SSLContextFactory.InvalidSSLConfig {
		String keystoreFilePath = getFilePath(keystore);
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFilePath, TEST_KEYSTORE_PASSWORD, KEYSTORE_TYPE, TEST_KEYSTORE_PASSWORD);

		String trustStoreFilePath = getFilePath(truststore);
		KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStoreFilePath, TEST_KEYSTORE_PASSWORD, TRUSTSTORE_TYPE);
		return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
	}

	public static String getFilePath(String jksTestResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(jksTestResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load test jks resource " + jksTestResource);
	}

}
