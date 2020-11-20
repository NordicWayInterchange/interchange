package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.jms.JMSException;
import javax.naming.NamingException;

public class BouvetDevReceiver {
	public static void main(String[] args) throws NamingException, JMSException {
		KeystoreDetails truststore = new KeystoreDetails("C:\\arbeid\\interchange\\jks\\node\\truststore.jks", "password", KeystoreType.JKS);
		KeystoreDetails keystore = new KeystoreDetails("C:\\arbeid\\interchange\\jks\\node\\secrets\\dev-bouvet.itsinterchange.eu\\dev-bouvet.p12", "password", KeystoreType.PKCS12, "password");

		Sink bouvetDevReceiver = new Sink("amqps://no-fed2.itsinterchange.eu", "dev-bouvet.itsinterchange.eu", SSLContextFactory.sslContextFromKeyAndTrustStores(keystore, truststore));
		bouvetDevReceiver.start();

	}

}
