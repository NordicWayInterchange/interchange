package no.vegvesen.ixn.napcore.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import no.vegvesen.ixn.napcore.model.*;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.impl.client.HttpClients;


import javax.net.ssl.SSLContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NapRESTClient {

    private RestTemplate restTemplate;

    private final String server;

    private final String user;

    private final String nap;

    public NapRESTClient(SSLContext sslContext, String server, String user, String nap) {
        this.restTemplate = new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(
                        HttpClients
                                .custom()
                                .setSSLContext(sslContext)
                                .build()
                )
        );
        this.server = server;
        this.user = user;
        this.nap = nap;
    }

    public CertificateSignResponse requestCertificate(CertificateSignRequest signingRequest) {
        String url = String.format("%s/nap/%s/x509/csr", server, user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CertificateSignRequest> entity = new HttpEntity<>(signingRequest, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, CertificateSignResponse.class).getBody();
    }

    public Subscription addSubscription(SubscriptionRequest subscriptionRequest) {
        String url = String.format("%s/nap/%s/subscriptions", server, user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SubscriptionRequest> entity = new HttpEntity<>(subscriptionRequest, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, Subscription.class).getBody();
    }

    public Subscription getSubscription(String subscriptionId) {
        String url = String.format("%s/nap/%s/subscriptions/%s", server, user, subscriptionId);
        return restTemplate.getForEntity(url, Subscription.class).getBody();
    }

    public List<Subscription> getSubscriptions() {
        String url = String.format("%s/nap/%s/subscriptions", server, user);
        ResponseEntity<Subscription[]> response = restTemplate.getForEntity(url, Subscription[].class);
        return Arrays.asList(response.getBody());
    }

    public void deleteSubscription(String subscriptionId) {
        restTemplate.delete(String.format("%s/nap/%s/subscriptions/%s", server, user, subscriptionId));
    }

    public List<Capability> getMatchingCapabilities(String selector) throws JsonProcessingException {
        String url = String.format("%s/nap/%s/subscriptions/capabilities", server, user);
        Map<String,String> parameters = new HashMap<>();
        parameters.put("selector",selector);
        ResponseEntity<Capability[]> response = restTemplate.getForEntity(url, Capability[].class, parameters);
        return Arrays.asList(response.getBody());
    }
}
