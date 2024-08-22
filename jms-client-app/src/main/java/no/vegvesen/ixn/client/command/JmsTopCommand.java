package no.vegvesen.ixn.client.command;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;

public interface JmsTopCommand {
    String getUrl();
    Path getKeystorePath();

    String getKeystorePassword();

    Path getTrustStorePath();

    String getTrustStorePassword();


    default SSLContext createContext() {
        KeystoreDetails keystoreDetails = new KeystoreDetails(getKeystorePath().toString(),
                getKeystorePassword(),
                KeystoreType.PKCS12);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(getTrustStorePath().toString(),
                getTrustStorePassword(), KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }
}
