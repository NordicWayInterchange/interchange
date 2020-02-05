package no.vegvesen.ixn.serviceprovider.client;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

//TODO this is hardcoded. Need to take it out into either properties or arbuments.
@Configuration
public class OnboardRESTClientConfig {



    @Bean
    public RestTemplate createRestTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(createHttpClient()));
    }

    private HttpClient createHttpClient() {
        return HttpClients.custom().setSSLContext(createSSLContext()).build();
    }

    private SSLContext createSSLContext() {
        return SSLContextFactory.sslContextFromKeyAndTrustStores(
                new KeystoreDetails("c:/interchange/tmp/keys/sp-one.bouvetinterchange.no.p12","password", KeystoreType.PKCS12,"password"),
                new KeystoreDetails("c:/interchange/tmp/keys/truststore.jks","password",KeystoreType.JKS));
    }
}
