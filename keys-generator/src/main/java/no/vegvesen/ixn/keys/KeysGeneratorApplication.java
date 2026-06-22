package no.vegvesen.ixn.keys;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.cert.KeyPairAndCsr;
import no.vegvesen.ixn.keys.generator.CARequest;
import no.vegvesen.ixn.keys.generator.CaResponse;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CertificateCertificateChainAndKeys;
import no.vegvesen.ixn.keys.generator.PasswordGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@SuppressWarnings("InstantiationOfUtilityClass")
@Command(name = "keygenerator",
        description = "Generates keys for Docker Compose tests",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        subcommands = {
                KeysGeneratorApplication.Keys.class,
                KeysGeneratorApplication.GenerateCsr.class,
                KeysGeneratorApplication.GenerateFromSigned.class
        }
)
public class KeysGeneratorApplication {


    @Command(
            name = "generate",
            description = "Generate keys from input JSON to target folder",
            mixinStandardHelpOptions = true
    )
    public static class Keys implements Callable<Integer> {
        @Option(names = "-f", required = true, description = "Path to input JSON file")
        private Path inputFile;

        @Option(names = "-o", required = true, description = "Folder for created key and truststores")
        private Path outputFolder;

        @Option(names = {"-s","--staticpassword"}, description = "Create a static password for testing. The password generated will be \"password\" for all keystores")
        private Boolean testPassword;



        @Override
        public Integer call() throws Exception {
            PasswordGenerator passwordGenerator;
            if (Objects.equals(testPassword, Boolean.TRUE)) {
                passwordGenerator = PasswordGenerator.staticPassword("password");
            } else {
                passwordGenerator = PasswordGenerator.random(new SecureRandom(),12);
            }
            if (!Files.isDirectory(outputFolder)) {
                throw new IllegalArgumentException("Output folder is not a directory");
            }
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<List<CARequest>> listTypeReference = new TypeReference<>() {};
            List<CARequest> caRequests = mapper.readValue(Files.newInputStream(inputFile), listTypeReference);
            List<CaResponse> caResponses = new ArrayList<>();
            for (CARequest request : caRequests) {
                CaResponse response = ClusterKeyGenerator.generate(request);
                caResponses.add(response);
            }
            for (CaResponse response : caResponses) {
                ClusterKeyGenerator.store(response,outputFolder,passwordGenerator);
            }
            return 0;
        }
    }

    /**
     * Generates a keypair and a PKCS10 CSR for a top-level CA that will be signed by an external
     * CA.  Two PEM files are written to the output directory:
     * <ul>
     *   <li>{@code <name>.keypair.pem} — private keypair (keep secret)</li>
     *   <li>{@code <name>.csr.pem}     — CSR to send to the external CA</li>
     * </ul>
     * After the external CA returns the signed certificate, use the {@code generate-from-signed}
     * command to build the rest of the certificate tree.
     */
    @Command(
            name = "generate-csr",
            description = "Generate a keypair and CSR for a top-level CA to be signed by an external CA",
            mixinStandardHelpOptions = true
    )
    public static class GenerateCsr implements Callable<Integer> {

        @Option(names = {"-n", "--name"}, required = true, description = "Common name (CN) for the top CA (e.g. ca.example.eu)")
        private String name;

        @Option(names = {"-c", "--country"}, description = "Two-letter ISO country code (default: NO)")
        private String country;

        @Option(names = "-o", required = true, description = "Output directory for the keypair and CSR PEM files")
        private Path outputFolder;

        @Override
        public Integer call() throws Exception {
            if (!Files.isDirectory(outputFolder)) {
                throw new IllegalArgumentException("Output folder is not a directory: " + outputFolder);
            }
            KeyPairAndCsr keyPairAndCsr = ClusterKeyGenerator.generateTopCaCsr(name, country);
            Path keypairPath = outputFolder.resolve(name + ".keypair.pem");
            Path csrPath = outputFolder.resolve(name + ".csr.pem");
            try (java.io.Writer keyWriter = Files.newBufferedWriter(keypairPath)) {
                ClusterKeyGenerator.saveKeyPair(keyPairAndCsr.keyPair(), keyWriter);
            }
            try (java.io.Writer csrWriter = Files.newBufferedWriter(csrPath)) {
                ClusterKeyGenerator.saveCSR(keyPairAndCsr.csr(), csrWriter);
            }
            System.out.println("Keypair written to: " + keypairPath);
            System.out.println("CSR written to:     " + csrPath);
            System.out.println("Send " + csrPath + " to the external CA for signing.");
            return 0;
        }
    }

    /**
     * Continues certificate tree generation after an external CA has signed the top CA CSR.
     * <p>
     * Reads the keypair PEM (written by {@code generate-csr}), the signed certificate PEM and
     * the external CA chain PEM, then generates all sub-CAs, host certificates and client
     * certificates described in the input JSON.  The top-level JKS truststore will contain the
     * external root CA certificate so that TLS clients can verify the full chain.
     * <p>
     * The input JSON must be a single {@link CARequest} object (not a list) describing the
     * sub-CAs, host and client certificates to generate under the externally-signed top CA.
     */
    @Command(
            name = "generate-from-signed",
            description = "Generate the certificate tree from an externally-signed top CA certificate",
            mixinStandardHelpOptions = true
    )
    public static class GenerateFromSigned implements Callable<Integer> {

        @Option(names = {"-k", "--key"}, required = true, description = "Path to the keypair PEM file produced by generate-csr")
        private Path keypairFile;

        @Option(names = {"--signed-cert"}, required = true, description = "Path to the signed certificate PEM returned by the external CA")
        private Path signedCertFile;

        @Option(names = {"--external-chain"}, required = true, description = "Path to the external CA chain PEM file (e.g. the external root CA certificate)")
        private Path externalChainFile;

        @Option(names = "-f", required = true, description = "Path to input JSON file (single CARequest describing sub-CAs, hosts and clients)")
        private Path inputFile;

        @Option(names = "-o", required = true, description = "Output directory for keystores and truststores")
        private Path outputFolder;

        @Option(names = {"-s", "--staticpassword"}, description = "Use the static password \"password\" for all keystores (for testing)")
        private Boolean testPassword;

        @Override
        public Integer call() throws Exception {
            if (!Files.isDirectory(outputFolder)) {
                throw new IllegalArgumentException("Output folder is not a directory: " + outputFolder);
            }

            // Load the keypair generated by generate-csr
            KeyPair keyPair;
            try (FileReader reader = new FileReader(keypairFile.toFile())) {
                keyPair = ClusterKeyGenerator.loadKeyPair(reader);
            }

            // Load the signed certificate returned by the external CA
            X509Certificate signedCert;
            try (FileReader reader = new FileReader(signedCertFile.toFile())) {
                signedCert = ClusterKeyGenerator.loadSingleCertificate(reader);
            }

            // Load the external CA chain (e.g. external root CA cert)
            List<X509Certificate> externalChain;
            try (FileReader reader = new FileReader(externalChainFile.toFile())) {
                externalChain = ClusterKeyGenerator.loadCertificateChain(reader);
            }

            // Build the full chain: signed top CA cert + external CA chain above it
            List<X509Certificate> fullChain = new ArrayList<>();
            fullChain.add(signedCert);
            fullChain.addAll(externalChain);

            CertificateCertificateChainAndKeys signedTopCa = new CertificateCertificateChainAndKeys(
                    keyPair,
                    signedCert,
                    fullChain
            );

            // The external trust anchor is the root of the external chain (last cert)
            X509Certificate externalTrustAnchor = externalChain.get(externalChain.size() - 1);

            // Parse the CARequest (single object, not a list)
            ObjectMapper mapper = new ObjectMapper();
            CARequest caRequest = mapper.readValue(Files.newInputStream(inputFile), CARequest.class);

            PasswordGenerator passwordGenerator;
            if (Objects.equals(testPassword, Boolean.TRUE)) {
                passwordGenerator = PasswordGenerator.staticPassword("password");
            } else {
                passwordGenerator = PasswordGenerator.random(new SecureRandom(), 12);
            }

            CaResponse response = ClusterKeyGenerator.generateFromSignedTopCa(caRequest, signedTopCa, new SecureRandom());
            ClusterKeyGenerator.store(response, outputFolder, passwordGenerator, externalTrustAnchor);
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new KeysGeneratorApplication()).execute(args);
        System.exit(exitCode);
    }
}
