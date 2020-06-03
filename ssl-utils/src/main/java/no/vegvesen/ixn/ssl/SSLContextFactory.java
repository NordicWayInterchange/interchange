package no.vegvesen.ixn.ssl;

/*-
 * #%L
 * ssl-utils
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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

@SuppressWarnings("WeakerAccess")
public class SSLContextFactory {
	public static SSLContext sslContextFromKeyAndTrustStores(KeystoreDetails keystoreDetails,
															 KeystoreDetails truststoreDetails){
		KeyStore keystore = loadKeystoreFromFile(keystoreDetails);
		KeyStore truststore = loadKeystoreFromFile(truststoreDetails);
		return newSSLContext(keystore, keystoreDetails.getKeyPassword(), truststore);
	}

	private static KeyStore loadKeystoreFromFile(KeystoreDetails details) {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(details.getType().toString());
			keyStore.load(new FileInputStream(details.getFileName()), details.getPassword().toCharArray());
		} catch (Throwable e1) {
			throw new InvalidSSLConfig(e1);
		}
		return keyStore;
	}

	private static SSLContext newSSLContext(final KeyStore ks, final String keyPassword, final KeyStore ts) {
		try {
			// Get a KeyManager and initialize it
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keyPassword.toCharArray());

			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);

			// Get the SSLContext to help create SSLSocketFactory
			final SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			return sslContext;
		} catch (final GeneralSecurityException e) {
			throw new InvalidSSLConfig(e);
		}
	}

	public static class InvalidSSLConfig extends RuntimeException {
		InvalidSSLConfig(Throwable e) {
			super(e);
		}
	}
}
