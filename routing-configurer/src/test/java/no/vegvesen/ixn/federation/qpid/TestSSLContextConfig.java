package no.vegvesen.ixn.federation.qpid;

/*-
 * #%L
 * routing-configurer
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.net.URL;

@Configuration
public class TestSSLContextConfig {

	@Value("${routing-configurer.ssl.trust-store-password}")
	String truststorePassword;
	@Value("${routing-configurer.ssl.trust-store-type}")
	String truststoreType;
	@Value("${routing-configurer.ssl.trust-store}")
	String truststoreName;

	@Value("${routing-configurer.ssl.key-store-type}")
	String keystoreType;
	@Value("${routing-configurer.ssl.key-store}")
	String keystoreName;
	@Value("${routing-configurer.ssl.key-store-password}")
	String keystorePassword;
	@Value("${routing-configurer.ssl.key-password}")
	String keyPassword;


	@Bean
	public SSLContext getTestSslContext() {
		String keystoreFileName = getFilePathFromClasspathResource(keystoreName);
		String truststoreFileName = getFilePathFromClasspathResource(truststoreName);
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(keystoreFileName, keystorePassword, KeystoreType.valueOf(keystoreType), keyPassword),
			new KeystoreDetails(truststoreFileName, truststorePassword, KeystoreType.valueOf(truststoreType)));
}

	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}


}
