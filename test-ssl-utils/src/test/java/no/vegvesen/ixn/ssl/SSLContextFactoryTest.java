package no.vegvesen.ixn.ssl;

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

import no.vegvesen.ixn.TestKeystoreHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SSLContextFactoryTest {

	@Test
	public void sslContextFromKeyAndTrustStores() {
		TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	}

	@Test
	public void sslContextFromKeyAndTrustStoresWrongKeystoreTypeFails() {
		String keystoreFile = TestKeystoreHelper.getFilePath("jks/localhost.p12");
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.JKS, "password");

		String filePath = TestKeystoreHelper.getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.PKCS12);

		assertThatExceptionOfType(SSLContextFactory.InvalidSSLConfig.class).isThrownBy(() -> {
			SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
		});
	}

	@Test
	public void sslContextFromKeyAndTrustStoresNonExistingFileFails() {
		String keystoreFile = "/non/existing/file/foobar.p12";
		KeystoreDetails keystoreDetails = new KeystoreDetails(keystoreFile, "password", KeystoreType.PKCS12, "password");

		String filePath = TestKeystoreHelper.getFilePath("jks/truststore.jks");
		KeystoreDetails truststoreDetails = new KeystoreDetails(filePath, "password", KeystoreType.JKS);

		assertThatExceptionOfType(SSLContextFactory.InvalidSSLConfig.class).isThrownBy(() -> {
			SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, truststoreDetails);
		});
	}


}
