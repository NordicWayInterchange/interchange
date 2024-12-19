package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.keys.generator.CARequest;
import no.vegvesen.ixn.keys.generator.CaResponse;
import no.vegvesen.ixn.keys.stores.CaStore;
import no.vegvesen.ixn.keys.stores.CaStores;
import no.vegvesen.ixn.keys.generator.ClientRequest;
import no.vegvesen.ixn.keys.stores.ClientStore;
import no.vegvesen.ixn.keys.generator.HostRequest;
import no.vegvesen.ixn.keys.stores.HostStore;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;

public class QpidDockerBaseIT extends DockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(QpidDockerBaseIT.class);

	public static QpidContainer getQpidTestContainer(CaStores stores, String vhostName, String hostname, Path configPath) {
		Path imageLocation = getFolderPath("qpid-test");
		logger.debug("Creating container qpid-it-memory, from Docker file from {} and config from {}",
				imageLocation, configPath);
		HostStore hostStore = stores.getHostStore(hostname);
		CaStore caStore = stores.trustStore();
        return new QpidContainer("qpid-it-memory",
				imageLocation,
				configPath,
				caStore.path().getParent(),
                hostStore.path().getFileName().toString(),
                hostStore.password(),
                caStore.path().getFileName().toString(),
                caStore.password(),
				vhostName);
	}

	public static CaStores generateStores(Path outputPath, String ca, String server, String ... serviceProviders) {
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<ClientRequest> clientRequests = new ArrayList<>();
		for (String serviceProvider : serviceProviders) {
			clientRequests.add(new ClientRequest(serviceProvider,"NO", serviceProvider + "@" + server));
		}
		CARequest request = new CARequest(
				ca,
				"NO",
				List.of(),
				List.of(new HostRequest(
						server
				)),
				clientRequests
		);
		CaResponse response;
		try {
            response = generate(request);
        } catch (CertificateException | NoSuchAlgorithmException | SignatureException | OperatorCreationException |
				 InvalidKeyException | NoSuchProviderException | CertIOException e) {
            throw new RuntimeException(e);
        }
        CaStores stores;
		try {
            stores = store(response,outputPath, () -> "password");
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
		return stores;
    }


	public static SSLContext sslClientContext(CaStores stores, String serviceProviderName) {
        ClientStore clientStore = stores.getClientStore(serviceProviderName);
		CaStore caStore = stores.trustStore();
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(
						clientStore.path().toString(),
						clientStore.password(),
						KeystoreType.PKCS12
				),
				new KeystoreDetails(
						caStore.path().toString(),
						caStore.password(),
						KeystoreType.JKS
				)
		);
	}

	public static SSLContext sslServerContext(CaStores stores, String hostName) {
		HostStore hostStore = stores.getHostStore(hostName);
		CaStore trustStore = stores.trustStore();
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(
						hostStore.path().toString(),
						hostStore.password(),
						KeystoreType.PKCS12
				),
				new KeystoreDetails(
						trustStore.path().toString(),
						trustStore.password(),
						KeystoreType.JKS
				)
		);
	}

}