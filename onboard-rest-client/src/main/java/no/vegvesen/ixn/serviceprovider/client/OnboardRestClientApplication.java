package no.vegvesen.ixn.serviceprovider.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public class OnboardRestClientApplication {

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.out.println("Not enough arguments");
            return;
        }
        String server = args[0];
        String user = args[1];
        String keystorePath = args[2];
        String keystorePassword = args[3];
        String keyPassword = args[4];
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        String trustStorePath = "/jks/truststore.jks";
        String trustStorePassword = "password";
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);

        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);

        OnboardRESTClient client = new OnboardRESTClient(sslContext,server,user);
        CapabilityApi serviceProviderCapabilities = client.getServiceProviderCapabilities();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(serviceProviderCapabilities));

    }

}
