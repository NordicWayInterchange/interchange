package no.vegvesen.ixn.napcore.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import no.vegvesen.ixn.napcore.model.*;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.impl.client.HttpClients;


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



    //TODO this is too simple. Need to also return the keys for the client to handle in some way.
    private String generateCSR(String serviceProviderName, String country) {
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
            StringWriter outputWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(outputWriter);
            pemWriter.writeObject(csr);
            pemWriter.close();
            return outputWriter.toString();

        } catch (NoSuchAlgorithmException | OperatorCreationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
