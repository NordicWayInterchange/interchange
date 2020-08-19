package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Properties;

public class JmsSource {

    /**
     * A message source. Sends a single message, and exits.
     * Note that the main file does not make use of any Spring Boot stuff.
     * However, an instance of the class could easily be used from Spring Boot, as all
     * dependent settings are handled in the main method, and passed as parameters to the instance.
     * @param args name-of-properties-file (optional)
     */
    public static void main(String[] args) throws NamingException, JMSException, IOException {
        Properties props = JmsProperties.getProperties(args, "/source.properties");
        String url = props.getProperty("source.url");
        String sendQueue = props.getProperty("source.sendQueue");
        String keystorePath = props.getProperty("source.keyStorepath") ;
        String keystorePassword = props.getProperty("source.keyStorepass");
        String keyPassword = props.getProperty("source.keypass");
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        String trustStorePath = props.getProperty("source.trustStorepath");
        String trustStorePassword = props.getProperty("source.trustStorepass");
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);

        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);

        try( Source s = new Source(url,sendQueue,sslContext)) {
            s.start();
            s.send("Yo!", "NO", ",someukquadtile");
        }
    }

}
