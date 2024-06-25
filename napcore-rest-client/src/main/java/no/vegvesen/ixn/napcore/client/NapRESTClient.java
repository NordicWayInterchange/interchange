package no.vegvesen.ixn.napcore.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.napcore.model.*;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


import javax.net.ssl.SSLContext;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NapRESTClient {

    private RestTemplate restTemplate;

    private final String server;

    private final String user;

    private final String nap;

    public NapRESTClient(SSLContext sslContext, String server, String user, String nap) {
        SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactoryBuilder
                .create()
                .setSslContext(sslContext)
                .build();
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();
        this.restTemplate = new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(
                        HttpClients
                                .custom()
                                .setConnectionManager(connectionManager)
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

    public Delivery addDelivery(DeliveryRequest deliveryRequest){
        String url = String.format("%s/nap/%s/deliveries", server, user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeliveryRequest> entity = new HttpEntity<>(deliveryRequest, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, Delivery.class).getBody();
    }

    public Delivery getDelivery(String deliveryId){
        String url = String.format("%s/nap/%s/deliveries/%s", server, user, deliveryId);
        return restTemplate.getForEntity(url, Delivery.class).getBody();
    }

    public List<Delivery> getDeliveries(){
        String url = String.format("%s/nap/%s/deliveries", server, user);
        ResponseEntity<Delivery[]> response = restTemplate.getForEntity(url, Delivery[].class);
        return Arrays.asList(response.getBody());
    }

    public void deleteDelivery(String deliveryId){
        String url = String.format("%s/nap/%s/deliveries/%s", server, user, deliveryId);
        restTemplate.delete(url);
    }

    public List<Capability> getMatchingDeliveryCapabilities(String selector){
        String url = String.format("%s/nap/%s/deliveries/capabilities");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("selector", selector);
        ResponseEntity<Capability[]> response = restTemplate.getForEntity(url, Capability[].class, parameters);
        return Arrays.asList(response.getBody());
    }

    public OnboardingCapability addCapability(CapabilitiesRequest request){
        String url = String.format("%s/nap/%s/capabilities", server, user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CapabilitiesRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, OnboardingCapability.class).getBody();
    }

    public OnboardingCapability getCapability(String capabilityId){
        String url = String.format("%s/nap/%s/capabilities/%s", server, user, capabilityId);
        return restTemplate.getForEntity(url, OnboardingCapability.class).getBody();
    }

    public List<OnboardingCapability> getCapabilities(){
        String url = String.format("%s/nap/%s/capabilities", server, user);
        ResponseEntity<OnboardingCapability[]> response = restTemplate.getForEntity(url, OnboardingCapability[].class);
        return Arrays.asList(response.getBody());
    }

    public void deleteCapability(String capabilityId){
        String url = String.format("%s/nap/%s/capabilities/%s", server, user, capabilityId);
        restTemplate.delete(url);
    }
    public KeyAndCSR generateKeyAndCSR(String serviceProviderName, String country) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            X500Principal x500Principal = new X500Principal(
                    String.format(
                            "emailAddress=test@test.com, CN=%s, O=Nordic Way, C=%s",
                            serviceProviderName,
                            country
                    )
            );
            JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(x500Principal, keyPair.getPublic());
            JcaContentSignerBuilder signBuilder = new JcaContentSignerBuilder("SHA256withRSA");
            ContentSigner signer = signBuilder.build(keyPair.getPrivate());
            PKCS10CertificationRequest csr = builder.build(signer);
            StringWriter csrWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(csrWriter);
            pemWriter.writeObject(csr);
            pemWriter.close();
            String csrString = csrWriter.toString();
            StringWriter keyWriter = new StringWriter();
            pemWriter = new JcaPEMWriter(keyWriter);
            pemWriter.writeObject(keyPair);
            pemWriter.close();
            String keyString = keyWriter.toString();
            return new KeyAndCSR(keyString,csrString);

        } catch (NoSuchAlgorithmException | OperatorCreationException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static class KeyAndCSR {
        private String key;

        private String csr;

        public KeyAndCSR(String key, String csr) {
            this.key = key;
            this.csr = csr;
        }


        public String getKey() {
            return key;
        }

        public String getCsr() {
            return csr;
        }
    }
}
