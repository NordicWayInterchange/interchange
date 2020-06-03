package no.vegvesen.ixn.federation.messagecollector;

/*-
 * #%L
 * message-collector
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

@Configuration
public class QpidQueueCollectorConfigurator {
    @Value("${collector.keystorepath}")
    private String keystorePath;

    @Value("${collector.keystorepassword}")
    private String keystorePassword;

    @Value("${collector.keystoretype}")
    private String keystoreType;

    @Value("${collector.truststorepath}")
    private String truststorePath;

    @Value("${collector.truststorepassword}")
    private String truststorePassword;

    @Value("${collector.truststoretype}")
    private String truststoreType;

    @Bean
    public SSLContext createSSLContext() {
        KeystoreDetails keystore = new KeystoreDetails(keystorePath,keystorePassword, KeystoreType.valueOf(keystoreType),keystorePassword);
        KeystoreDetails trustStore = new KeystoreDetails(truststorePath,truststorePassword,KeystoreType.valueOf(truststoreType));
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystore,trustStore);
    }
}
