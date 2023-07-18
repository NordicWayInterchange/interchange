package no.vegvesen.ixn.docker;

import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.CertificateAndCertificateChain;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.KeyPairAndCertificate;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.KeyPairAndCsr;
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

public class DockerBaseIT {
	private static Logger logger = LoggerFactory.getLogger(DockerBaseIT.class);
	private static final String CI_WORKDIR = "CIRCLE_WORKING_DIRECTORY";

	protected static Path getFolderPath(String relativePathFromProjectRoot) {
		Path projectRoot = getProjectRootPath();
		logger.debug("Resolving path to project folder [{}] from project root path: [{}]", relativePathFromProjectRoot, projectRoot.toString());
		Path dockerFilePath = projectRoot.resolve(relativePathFromProjectRoot);
		logger.debug("Resolved path to project folder [{}] to path [{}]", relativePathFromProjectRoot, dockerFilePath.toAbsolutePath().toString());
		return dockerFilePath;
	}

	private static Path getProjectRootPath() {
		String projectFolder = "interchange";

		String ciWorkdir = System.getenv(CI_WORKDIR);
		if (ciWorkdir != null) {
			logger.debug("Circle CI because we got environment varaible [{}] with value [{}]", CI_WORKDIR, ciWorkdir);
			Path ciWorkdirPath = Paths.get(ciWorkdir);
			logger.debug("ci workdir environment variable [{}] with folder name [{}] reolves to full path [{}]", ciWorkdir, ciWorkdirPath.getFileName().toString(), ciWorkdirPath.toAbsolutePath());
			projectFolder = ciWorkdirPath.getFileName().toString();
		}

		Path run = Paths.get(".").toAbsolutePath();
		Path projectRoot = null;
		while (projectRoot == null && run.getParent() != null) {
			if (run.endsWith(projectFolder)) {
				projectRoot = run;
			}
			run = run.getParent();
		}
		if (projectRoot == null ) {
			throw new RuntimeException("Could not resolve path to project root in parent folder of " + run.toString());
		}
		return projectRoot;
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
			KeyPairAndCertificate topCa = ClusterKeyGenerator.generateTopCa(ca, "NO");
			ClusterKeyGenerator.makeTrustStore(truststorePassword,"myKey",topCa.getCertificate(),new FileOutputStream(truststorePath.toFile()));
			KeyPairAndCsr intermediate = ClusterKeyGenerator.generateIntermediateKeypairAndCsr(server, "NO");
			CertificateAndCertificateChain intermediateCertDetails = ClusterKeyGenerator.signIntermediateCsr(topCa.getCertificate(),topCa.getKeyPair() , intermediate.getCsr());
			ClusterKeyGenerator.generateKeystoreBC(keystorePassword,server,intermediate.getKeyPair().getPrivate(),intermediateCertDetails.getChain().toArray(new X509Certificate[0]), keysOutputPath.resolve(serverKeystoreName));
			for (String serviceProvider : serviceProviders) {
				KeyPairAndCsr spKeys = ClusterKeyGenerator.generateCsrForServiceProviderBC(serviceProvider, "NO");
				List<X509Certificate> certificates = ClusterKeyGenerator.generateServiceProviderCertBC(spKeys.getCsr(), topCa.getCertificate(), intermediateCertDetails.getCertificate(), intermediate.getKeyPair().getPrivate(), serviceProvider);
				String spKeystoreName = serviceProvider + ".p12";
				ClusterKeyGenerator.generateKeystoreBC("password",serviceProvider,spKeys.getKeyPair().getPrivate(),certificates.toArray(new X509Certificate[0]), keysOutputPath.resolve(spKeystoreName));
				spKeystoreNames.put(serviceProvider,spKeystoreName);
			}

		} catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException | KeyStoreException |
				 IOException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
			throw new RuntimeException(e);
		}
		//TODO this does not allow for individual passwords for SP-keystores
		return new KeysStructure(keysOutputPath, serverKeystoreName, truststoreName, spKeystoreNames, truststorePassword,keystorePassword);
	}

	public static Path getTargetFolderPathForTestClass(Class clazz) {
		return getFolderPath("target/test-keys-" + clazz.getSimpleName());
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
