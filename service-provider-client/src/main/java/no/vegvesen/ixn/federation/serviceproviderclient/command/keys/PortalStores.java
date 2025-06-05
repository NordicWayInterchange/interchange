package no.vegvesen.ixn.federation.serviceproviderclient.command.keys;

import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "portalstore",
        description = "Create keystore based on keys and certs from the portal",
        mixinStandardHelpOptions = true,
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient portalstore king_olav.bouvetinterchange.eu inputfolder outputfolder
                        """
        }
)
public class PortalStores implements Callable<Integer> {

    @Parameters(
            index = "0",
            description = "Common name (can be found on the front page of the portal)"
    )
    private String commonName;

    @Parameters(
            index = "1",
            description = "Folder where the pem files may be found"
    )
    private Path inputFolder;

    @Parameters(
            index = "2",
            description = "Folder where the resulting keystore and truststore are to be stored"
    )
    private Path outputFolder;

    @Override
    public Integer call() throws Exception {
        String keyName = commonName + ".key.pem";
        String chainName = "chain." + commonName + ".crt.pem";
        String rootCertificateName = "root." + commonName + ".crt.pem";

        PrivateKey privateKey = ClusterKeyGenerator.loadPrivateKey(Files.newBufferedReader(inputFolder.resolve(keyName)));
        List<X509Certificate> certificateChain = ClusterKeyGenerator.loadCertificateChain(Files.newBufferedReader(inputFolder.resolve(chainName)));
        X509Certificate rootCertificate = ClusterKeyGenerator.loadSingleCertificate(Files.newBufferedReader(inputFolder.resolve(rootCertificateName)));

        String keystoreName = commonName + ".p12";
        ClusterKeyGenerator.makeKeystore(commonName, "password", Files.newOutputStream(outputFolder.resolve(keystoreName)), certificateChain, privateKey);
        String truststoreName = "truststore" + commonName + ".jks";
        ClusterKeyGenerator.makeTrustStore("password", Files.newOutputStream(outputFolder.resolve(truststoreName)), rootCertificate, "myKey");
        return 0;
    }
}
