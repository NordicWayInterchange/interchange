package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
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
import java.util.HashMap;
import java.util.List;

public class QpidDockerBaseIT extends DockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(QpidDockerBaseIT.class);

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
			ClusterKeyGenerator.KeyPairAndCertificate topCa = ClusterKeyGenerator.generateTopCa(ca, "NO");
			ClusterKeyGenerator.makeTrustStore(truststorePassword,"myKey",topCa.getCertificate(),new FileOutputStream(truststorePath.toFile()));
			ClusterKeyGenerator.KeyPairAndCsr intermediate = ClusterKeyGenerator.generateIntermediateKeypairAndCsr(server, "NO");
			ClusterKeyGenerator.CertificateAndCertificateChain intermediateCertDetails = ClusterKeyGenerator.signIntermediateCsr(topCa.getCertificate(),topCa.getKeyPair() , intermediate.getCsr());
			Path serverKeystorePath = keysOutputPath.resolve(serverKeystoreName);
			ClusterKeyGenerator.generateKeystoreBC(keystorePassword,server,intermediate.getKeyPair().getPrivate(),intermediateCertDetails.getChain().toArray(new X509Certificate[0]), new FileOutputStream(serverKeystorePath.toFile()));
			for (String serviceProvider : serviceProviders) {
				ClusterKeyGenerator.KeyPairAndCsr spKeys = ClusterKeyGenerator.generateCsrForServiceProviderBC(serviceProvider, "NO");
				List<X509Certificate> certificates = ClusterKeyGenerator.generateServiceProviderCertBC(spKeys.getCsr(), topCa.getCertificate(), intermediateCertDetails.getCertificate(), intermediate.getKeyPair().getPrivate(), serviceProvider);
				String spKeystoreName = serviceProvider + ".p12";
				Path spKeystorePath = keysOutputPath.resolve(spKeystoreName);
				String spKeystorePassword = "password";
				ClusterKeyGenerator.generateKeystoreBC(spKeystorePassword,serviceProvider,spKeys.getKeyPair().getPrivate(),certificates.toArray(new X509Certificate[0]), new FileOutputStream(spKeystorePath.toFile()));
				spKeystoreNames.put(serviceProvider,spKeystoreName);
				spKeystorePasswords.put(serviceProvider,spKeystorePassword);
			}

		} catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException | KeyStoreException |
				 IOException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
			throw new RuntimeException(e);
		}
		return new KeysStructure(keysOutputPath, serverKeystoreName, truststoreName, spKeystoreNames, spKeystorePasswords, truststorePassword,keystorePassword);
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

		public HashMap<String, String> getSpKeystoreNames() {
			return spKeystoreNames;
		}

		public String getSpKeystoreName(String spName) {
			return spKeystoreNames.get(spName);
		}

		public HashMap<String, String> getSpKeystorePasswords() {
			return spKeystorePasswords;
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