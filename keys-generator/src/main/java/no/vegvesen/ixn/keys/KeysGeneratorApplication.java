package no.vegvesen.ixn.keys;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
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
        customSynopsis = {
                """
                        Examples:\n
                        keygenerator generate -f input.json -o /path/to/output
                        keygenerator generate-csr -n ca.example.eu -o /path/to/output
                        keygenerator generate-from-signed -k ca.keypair.pem --signed-cert ca.signed.pem --external-chain external-root-ca.pem -f input.json -o /path/to/output
                        """
        },
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
            mixinStandardHelpOptions = true,
            customSynopsis = {
                    """
                            Examples:\n
                            keygenerator generate -f input.json -o /path/to/output
                            keygenerator generate -f input.json -o /path/to/output -s
                            """
            }
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

    @Command(
            name = "generate-csr",
            description = "Generate a keypair and CSR for a top-level CA to be signed by an external CA",
            mixinStandardHelpOptions = true,
            customSynopsis = {
                    """
                            Examples:\n
                            keygenerator generate-csr -n ca.example.eu -o /path/to/output
                            keygenerator generate-csr --name ca.example.eu --country NO -o /path/to/output
                            """
            }
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

    @Command(
            name = "generate-from-signed",
            description = "Generate the certificate tree from an externally-signed top CA certificate",
            mixinStandardHelpOptions = true,
            customSynopsis = {
                    """
                            Examples:\n
                            keygenerator generate-from-signed -k ca.keypair.pem --signed-cert ca.signed.pem --external-chain external-root-ca.pem -f input.json -o /path/to/output
                            keygenerator generate-from-signed -k ca.keypair.pem --signed-cert ca.signed.pem --external-chain external-root-ca.pem -f input.json -o /path/to/output -s
                            """
            }
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

            KeyPair keyPair;
            try (FileReader reader = new FileReader(keypairFile.toFile())) {
                keyPair = ClusterKeyGenerator.loadKeyPair(reader);
            }

            X509Certificate signedCert;
            try (FileReader reader = new FileReader(signedCertFile.toFile())) {
                signedCert = ClusterKeyGenerator.loadSingleCertificate(reader);
            }

            List<X509Certificate> externalChain;
            try (FileReader reader = new FileReader(externalChainFile.toFile())) {
                externalChain = ClusterKeyGenerator.loadCertificateChain(reader);
            }

            List<X509Certificate> fullChain = new ArrayList<>();
            fullChain.add(signedCert);
            fullChain.addAll(externalChain);

            CertificateCertificateChainAndKeys signedTopCa = new CertificateCertificateChainAndKeys(
                    keyPair,
                    signedCert,
                    fullChain
            );

            X509Certificate externalTrustAnchor = externalChain.get(externalChain.size() - 1);

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
