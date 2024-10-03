package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.keys.generator.*;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStore;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.ClientStore;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;

public class QpidDockerBaseIT extends DockerBaseIT {


	public static QpidContainer getQpidTestContainer(CaStores stores, String vhostName, String hostname, Path configPath) {
		Stream<HostStore> stream = stores.hostStores().stream();
		HostStore hostStore = getHostStore(hostname, stream);
		CaStore caStore = stores.trustStore();
		String keystoreName = hostStore.path().getFileName().toString();
		String keystorePassword = hostStore.password();
		String truststoreName = caStore.path().getFileName().toString();
		String truststorePassword = caStore.password();
		return new QpidContainer(
				configPath,
				caStore.path().getParent(),
				keystoreName,
				keystorePassword,
				truststoreName,
				truststorePassword,
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
        ClientStore clientStore = getClientStore(serviceProviderName, stores.clientStores().stream());
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

	public static String getTrustStorePath(CaStores stores) {
		return stores.trustStore().path().toString();
	}

	public static String getClientStorePath(String clientName, List<ClientStore> clientStores) {
		return getClientStore(clientName,clientStores.stream()).path().toString();

	}

	public static SSLContext sslServerContext(CaStores stores, String hostName) {
		HostStore hostStore = getHostStore(hostName, stores.hostStores().stream());
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

	private static HostStore getHostStore(String hostname, Stream<HostStore> stream) {
		return stream.filter(h -> h.hostname().equals(hostname)).findAny().orElseThrow(() -> new RuntimeException("No store found for hostname: " + hostname));
	}

	private static ClientStore getClientStore(String serviceProviderName, Stream<ClientStore> stream) {
		return stream.filter(c -> c.clientName().equals(serviceProviderName)).findAny().orElseThrow(() -> new RuntimeException("No client store found for " + serviceProviderName));
	}

}