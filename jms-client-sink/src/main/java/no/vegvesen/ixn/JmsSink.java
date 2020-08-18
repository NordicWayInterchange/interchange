package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.net.ssl.SSLContext;
import java.util.Properties;

public class JmsSink {
    	public static void main(String[] args) throws Exception {
		Properties props = JmsProperties.getProperties(args, "/sink.properties");

		String url = props.getProperty("sink.url");
        String receiveQueue = props.getProperty("sink.receiveQueue");
        String keystorePath = props.getProperty("sink.keyStorepath");
        String keystorePassword = props.getProperty("sink.keyStorepass");
        String keyPassword = props.getProperty("sink.keypass");
        String trustStorePath = props.getProperty("sink.trustStorepath");
        String truststorePassword = props.getProperty("sink.trustStorepass");

        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                truststorePassword,KeystoreType.JKS);

        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);

        Sink sink = new Sink(url,receiveQueue,sslContext);
		System.out.println(String.format("Listening for messages from queue [%s] on server [%s]", receiveQueue, url));
        sink.start();
    }

}
