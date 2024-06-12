package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.keygen.generator.*;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.*;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class QpidDockerBaseIT extends DockerBaseIT {

	private static final Logger logger = LoggerFactory.getLogger(QpidDockerBaseIT.class);

	public static QpidContainer getQpidTestContainer(String configPathFromClasspath, KeysStructure keysStructure, String vhostName) {
		Path imageLocation = getFolderPath("qpid-test");
		Path configPath = Paths.get(configPathFromClasspath); //TODO parameter
		logger.debug("Creating container qpid-it-memory, from Docker file from {} and config from {}",
				imageLocation,configPath);
		return new QpidContainer("qpid-it-memory",
				imageLocation,
				configPath,
				keysStructure.getKeysOutputPath(),
				keysStructure.getServerKeystoreName(),
				keysStructure.getKeystorePassword(),
				keysStructure.getTruststoreName(),
				keysStructure.getTruststorePassword(),
				vhostName
		);
	}


	public static QpidContainer getQpidTestContainer(Path configPath, CaStores stores, String vhostName, String hostname) {
		Path imageLocation = getFolderPath("qpid-test");
		logger.debug("Creating container qpid-it-memory, from Docker file from {} and config from {}",
				imageLocation, configPath);

		Stream<HostStore> stream = stores.hostStores().stream();
		HostStore hostStore = getHostStore(hostname, stream);
		CaStore caStore = stores.trustStore();
		String keystoreName = hostStore.path().getFileName().toString();
		String keystorePassword = hostStore.password();
		String truststoreName = caStore.path().getFileName().toString();
		String truststorePassword = caStore.password();
		return new QpidContainer("qpid-it-memory",
				imageLocation,
				configPath,
				caStore.path().getParent(),
				keystoreName,
				keystorePassword,
				truststoreName,
				truststorePassword,
				vhostName
		);
	}

	public static CaStores generateStores(Path outputPath, String ca, String server, String ... serviceProviders) {
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
            response = ClusterKeyGenerator.generate(request);
        } catch (CertificateException | NoSuchAlgorithmException | SignatureException | OperatorCreationException |
                 InvalidKeyException | NoSuchProviderException | CertIOException e) {
            throw new RuntimeException(e);
        }
        CaStores stores;
		try {
            stores = ClusterKeyGenerator.store(response,outputPath, () -> "password");
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
		return stores;
    }

	public static KeysStructure generateKeys(Class<?> clazz,String ca, String server, String ... serviceProviders) {
		Path keysOutputPath = getTargetFolderPathForTestClass(clazz);
		try {
			Files.createDirectories(keysOutputPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String truststoreName = "truststore.jks";
		Path truststorePath = keysOutputPath.resolve(truststoreName);
		HashMap<String,String> spKeystoreNames = new HashMap<>();
		HashMap<String,String> spKeystorePasswords = new HashMap<>();
		String serverKeystoreName = server + ".p12";
		String keystorePassword = "password";
		String truststorePassword = "password";
		try {
			SecureRandom secureRandom = new SecureRandom();
			String countryCode = "NO";
			CertificateCertificateChainAndKeys topCa = ClusterKeyGenerator.generateTopCa( ca, countryCode, secureRandom);
			ClusterKeyGenerator.makeTrustStore(truststorePassword,"myKey",topCa.certificate(),new FileOutputStream(truststorePath.toFile()));
			CertificateCertificateChainAndKeys host = ClusterKeyGenerator.generateServerCertForHost(server, topCa.certificate(), topCa.certificateChain(), topCa.keyPair().getPrivate(), secureRandom);
			Path serverKeystorePath = keysOutputPath.resolve(serverKeystoreName);
			ClusterKeyGenerator.generateKeystore(keystorePassword,server,host.keyPair().getPrivate(),host.certificateChain(), new FileOutputStream(serverKeystorePath.toFile()));
			CertSigner certSigner = new CertSigner(topCa.keyPair().getPrivate(),topCa.certificate(),topCa.certificateChain());
			for (String serviceProvider : serviceProviders) {
				ClusterKeyGenerator.KeyPairAndCsr spKeys = ClusterKeyGenerator.generateCsrForServiceProviderBC(serviceProvider, countryCode, "test@test.com");
				List<X509Certificate> certificates = certSigner.sign(spKeys.csr(), serviceProvider);
				String spKeystoreName = serviceProvider + ".p12";
				Path spKeystorePath = keysOutputPath.resolve(spKeystoreName);
				String spKeystorePassword = "password";
				ClusterKeyGenerator.generateKeystore(spKeystorePassword,serviceProvider,spKeys.keyPair().getPrivate(),certificates, new FileOutputStream(spKeystorePath.toFile()));
				spKeystoreNames.put(serviceProvider,spKeystoreName);
				spKeystorePasswords.put(serviceProvider,spKeystorePassword);
			}

		} catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException | KeyStoreException |
				 IOException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
			throw new RuntimeException(e);
		}
		return new KeysStructure(keysOutputPath, serverKeystoreName, truststoreName, spKeystoreNames, spKeystorePasswords, truststorePassword,keystorePassword);
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

	private static HostStore getHostStore(String hostname, Stream<HostStore> stream) {
		return stream.filter(h -> h.hostname().equals(hostname)).findAny().orElseThrow(() -> new RuntimeException("No store found for hostname: " + hostname));
	}

	private static ClientStore getClientStore(String serviceProviderName, Stream<ClientStore> stream) {
		return stream.filter(c -> c.clientName().equals(serviceProviderName)).findAny().orElseThrow(() -> new RuntimeException("No client store found for " + serviceProviderName));
	}

	public static SSLContext sslClientContext(KeysStructure keysStructure, String spName) {
		Path keysOutputPath = keysStructure.getKeysOutputPath();
		Path trustStorePath = keysOutputPath.resolve(keysStructure.getTruststoreName());
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(
						keysStructure.getKeysOutputPath().resolve(keysStructure.getSpKeystoreName(spName)).toString(),
						keysStructure.getSpKeystorePassword(spName),
						KeystoreType.PKCS12
				),
				new KeystoreDetails(
						trustStorePath.toString(),
						keysStructure.getTruststorePassword(),
						KeystoreType.JKS
				)
		);
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

	public static SSLContext sslServerContext(KeysStructure keysStructure) {
		Path basePath = keysStructure.getKeysOutputPath();
		KeystoreDetails keystoreDetails = new KeystoreDetails(
				basePath.resolve(keysStructure.getServerKeystoreName()).toString(),
				keysStructure.getKeystorePassword(),
				KeystoreType.PKCS12
		);
		KeystoreDetails truststoreDetails = new KeystoreDetails(
				basePath.resolve(keysStructure.getTruststoreName()).toString(),
				keysStructure.getTruststorePassword(),
				KeystoreType.JKS
		);
		return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails,truststoreDetails);

	}

	public static class KeysStructure {

		private final Path keysOutputPath;
		private final String truststoreName;
		private final String serverKeystoreName;
		private final HashMap<String, String> spKeystoreNames;

		private final HashMap<String, String> spKeystorePasswords;

		private final String truststorePassword;
		private final String keystorePassword;

		public KeysStructure(Path keysOutputPath,
							 String serverKeystoreName,
							 String truststoreName,
							 HashMap<String, String> spKeystoreNames,
							 HashMap<String, String> spKeystorePasswords, 
							 String truststorePassword,
							 String keystorePassword) {

			this.keysOutputPath = keysOutputPath;
			this.truststoreName = truststoreName;
			this.serverKeystoreName = serverKeystoreName;
			this.spKeystoreNames = spKeystoreNames;
			this.spKeystorePasswords = spKeystorePasswords;
			this.truststorePassword = truststorePassword;
			this.keystorePassword = keystorePassword;
		}

		public Path getKeysOutputPath() {
			return keysOutputPath;
		}

		public String getTruststoreName() {
			return truststoreName;
		}

		public String getServerKeystoreName() {
			return serverKeystoreName;
		}

		public String getSpKeystoreName(String spName) {
			return spKeystoreNames.get(spName);
		}

		public String getSpKeystorePassword(String spName) {
			return spKeystorePasswords.get(spName);
		}

		public String getTruststorePassword() {
			return truststorePassword;
		}

		public String getKeystorePassword() {
			return keystorePassword;
		}
	}
}