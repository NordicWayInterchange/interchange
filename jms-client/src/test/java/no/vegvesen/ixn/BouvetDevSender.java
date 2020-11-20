package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.jms.JMSException;
import javax.naming.NamingException;

public class BouvetDevSender {

	public static void main(String[] args) throws JMSException, NamingException {
		KeystoreDetails truststore = new KeystoreDetails("C:\\arbeid\\interchange\\jks\\node\\truststore.jks", "password", KeystoreType.JKS);
		KeystoreDetails keystore = new KeystoreDetails("C:\\arbeid\\interchange\\jks\\node\\secrets\\dev-bouvet.itsinterchange.eu\\dev-bouvet.p12", "password", KeystoreType.PKCS12, "password");

		Source bouvetDevSslSource = new Source("amqps://no-fed2.itsinterchange.eu", "onramp", SSLContextFactory.sslContextFromKeyAndTrustStores(keystore, truststore));
		bouvetDevSslSource.start();

		for (int i = 0; i < 5; i++) {
			bouvetDevSslSource.sendNorwegianTestMessage("Testing! One, two, three! Mjøsa! Testing!");
		}
		bouvetDevSslSource.close();
	}

}
