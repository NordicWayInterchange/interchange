package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				ClusterKeyGenerator.generateKeystoreBC("password",serviceProvider,spKeys.getKeyPair().getPrivate(),certificates.toArray(new X509Certificate[0]), new FileOutputStream(spKeystorePath.toFile()));
				spKeystoreNames.put(serviceProvider,spKeystoreName);
			}

		} catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException | KeyStoreException |
				 IOException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
			throw new RuntimeException(e);
		}
		//TODO this does not allow for individual passwords for SP-keystores
		return new KeysStructure(keysOutputPath, serverKeystoreName, truststoreName, spKeystoreNames, truststorePassword,keystorePassword);
	}

	public static class KeysStructure {

		private final Path keysOutputPath;
		private final String truststoreName;
		private final String serverKeystoreName;
		private final HashMap<String, String> spKeystoreNames;
		private final String truststorePassword;
		private final String keystorePassword;

		public KeysStructure(Path keysOutputPath,
							 String serverKeystoreName,
							 String truststoreName,
							 HashMap<String, String> spKeystoreNames,
							 String truststorePassword,
							 String keystorePassword) {

			this.keysOutputPath = keysOutputPath;
			this.truststoreName = truststoreName;
			this.serverKeystoreName = serverKeystoreName;
			this.spKeystoreNames = spKeystoreNames;
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

		public String getTruststorePassword() {
			return truststorePassword;
		}

		public String getKeystorePassword() {
			return keystorePassword;
		}
	}
}