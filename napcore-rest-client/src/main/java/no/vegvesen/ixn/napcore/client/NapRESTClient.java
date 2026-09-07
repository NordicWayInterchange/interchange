package no.vegvesen.ixn.napcore.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.cert.CsrGenerator;
import no.vegvesen.ixn.cert.KeyPairAndCsr;
import no.vegvesen.ixn.napcore.model.*;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class NapRESTClient {

    private final RestTemplate restTemplate;

    private final String server;

    private final String user;

    private final String nap;

    public NapRESTClient(SSLContext sslContext, String server, String user, String nap) {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setTlsSocketStrategy(
                        ClientTlsStrategyBuilder.create()
                                .setSslContext(sslContext)
                                .buildClassic()
                )
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
        String url = String.format("%s/nap/%s/subscriptions/capabilities?selector={selector}", server, user);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("selector", selector);
        ResponseEntity<Capability[]> response = restTemplate.getForEntity(url, Capability[].class, parameters);
        return Arrays.asList(response.getBody());
    }

    public Delivery addDelivery(DeliveryRequest deliveryRequest) {
        String url = String.format("%s/nap/%s/deliveries", server, user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeliveryRequest> entity = new HttpEntity<>(deliveryRequest, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, Delivery.class).getBody();
    }

    public Delivery getDelivery(String deliveryId) {
        String url = String.format("%s/nap/%s/deliveries/%s", server, user, deliveryId);
        return restTemplate.getForEntity(url, Delivery.class).getBody();
    }

    public List<Delivery> getDeliveries() {
        String url = String.format("%s/nap/%s/deliveries", server, user);
        ResponseEntity<Delivery[]> response = restTemplate.getForEntity(url, Delivery[].class);
        return Arrays.asList(response.getBody());
    }

    public void deleteDelivery(String deliveryId) {
        String url = String.format("%s/nap/%s/deliveries/%s", server, user, deliveryId);
        restTemplate.delete(url);
    }

    public List<Capability> getMatchingDeliveryCapabilities(String selector) {
        String url = String.format("%s/nap/%s/deliveries/capabilities?selector={selector}", server, user);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("selector", selector);
        ResponseEntity<Capability[]> response = restTemplate.getForEntity(url, Capability[].class, parameters);
        return Arrays.asList(response.getBody());
    }

    public OnboardingCapability addCapability(CapabilitiesRequest request) {
        String url = String.format("%s/nap/%s/capabilities", server, user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CapabilitiesRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, OnboardingCapability.class).getBody();
    }

    public OnboardingCapability getCapability(String capabilityId) {
        String url = String.format("%s/nap/%s/capabilities/%s", server, user, capabilityId);
        return restTemplate.getForEntity(url, OnboardingCapability.class).getBody();
    }

    public List<OnboardingCapability> getCapabilities() {
        String url = String.format("%s/nap/%s/capabilities", server, user);
        ResponseEntity<OnboardingCapability[]> response = restTemplate.getForEntity(url, OnboardingCapability[].class);
        return Arrays.asList(response.getBody());
    }

    public Set<String> getPublicationIds() {
        String url = String.format("%s/nap/%s/capabilities/publicationids", server, user);
        ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);
        return Arrays.stream(response.getBody()).collect(Collectors.toSet());
    }

    public void deleteCapability(String capabilityId) {
        String url = String.format("%s/nap/%s/capabilities/%s", server, user, capabilityId);
        restTemplate.delete(url);
    }

    public PrivateChannelResponse addPrivateChannel(PrivateChannelRequest privateChannelRequest) {
        String url = String.format("%s/nap/%s/privatechannels", server, user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PrivateChannelRequest> entity = new HttpEntity<>(privateChannelRequest, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, PrivateChannelResponse.class).getBody();
    }

    public void deletePrivateChannel(String privateChannelId) {
        String url = String.format("%s/nap/%s/privatechannels/%s", server, user, privateChannelId);
        restTemplate.delete(url);
    }

    public List<PrivateChannelResponse> getPrivateChannels() {
        String url = String.format("%s/nap/%s/privatechannels", server, user);
        ResponseEntity<PrivateChannelResponse[]> response = restTemplate.getForEntity(url, PrivateChannelResponse[].class);
        return Arrays.asList(response.getBody());
    }

    public PrivateChannelResponse getPrivateChannel(String privateChannelId) {
        String url = String.format("%s/nap/%s/privatechannels/%s", server, user, privateChannelId);
        return restTemplate.getForEntity(url, PrivateChannelResponse.class).getBody();
    }

    public List<PeerPrivateChannel> getPeerPrivateChannels() {
        String url = String.format("%s/nap/%s/privatechannels/peer", server, user);
        ResponseEntity<PeerPrivateChannel[]> response = restTemplate.getForEntity(url, PeerPrivateChannel[].class);
        return Arrays.asList(response.getBody());
    }

    public void addPeerToPrivateChannel(String privateChannelId, AddPeerRequest peerRequest) {
        String url = String.format("%s/nap/%s/privatechannels/peer/%s", server, user, privateChannelId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddPeerRequest> entity = new HttpEntity<>(peerRequest, headers);
        restTemplate.exchange(url, HttpMethod.PATCH, entity, AddPeerRequest.class);
    }

    public void deletePeerFromPrivateChannel(String privateChannelId, String peerName) {
        String url = String.format("%s/nap/%s/privatechannels/peer/%s/%s", server, user, privateChannelId, peerName);
        restTemplate.delete(url);
    }

    public void peerDeletePeerFromPrivateChannel(String privateChannelId) {
        String url = String.format("%s/nap/%s/privatechannels/peer/%s", server, user, privateChannelId);
        restTemplate.delete(url);
    }

    public KeyPairAndCsr generateKeyAndCSR(String serviceProviderName, String country) {
        try {
            X500Name x500Name = new X500Name(
                    String.format(
                            "emailAddress=%s, CN=%s, O=Nordic Way, C=%s",
                            serviceProviderName + "@test.com",
                            serviceProviderName,
                            country
                    )
            );
            return new CsrGenerator("RSA", 2048, "SHA512withRSA").generateKeyPairAndCsr(x500Name);
        } catch (NoSuchAlgorithmException | OperatorCreationException e) {
            throw new RuntimeException(e);
        }
    }

}
