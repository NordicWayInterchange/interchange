package no.vegvesen.ixn.federation.serviceproviderclient;

import no.vegvesen.ixn.federation.serviceproviderclient.command.auth.LoginCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.auth.LogoutCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.biconsumer.BiconsumerCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.biqueue.BiqueueCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities.CapabilitiesCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries.DeliveriesCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.keys.PortalStores;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.PrivateChannelsCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions.SubscriptionsCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.token.FetchTokenCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.token.NotAuthenticatedException;
import no.vegvesen.ixn.federation.serviceproviderclient.token.StoredToken;
import no.vegvesen.ixn.federation.serviceproviderclient.token.TokenStore;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator;
import no.vegvesen.ixn.ssl.InvalidSSLConfig;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static picocli.CommandLine.*;

@Command(name = "serviceproviderclient",
        description = "Service provider client",
        showAtFileInUsageHelp = true,
        defaultValueProvider = PropertiesDefaultProvider.class,
        subcommands = {
                LoginCommand.class,
                LogoutCommand.class,
                BiconsumerCommand.class,
                BiqueueCommand.class,
                CapabilitiesCommand.class,
                DeliveriesCommand.class,
                SubscriptionsCommand.class,
                PrivateChannelsCommand.class,
                PortalStores.class,
                FetchTokenCommand.class
        },
        mixinStandardHelpOptions = true,
        version = "1.0")
public class ServiceProviderClientApplication{

    @Parameters(index = "0", paramLabel = "SERVER", description = "URL to connect to")
    private String server;

    @Option(names = {"-u", "--user"}, required = true, description = "The service provider user")
    private String user;

    static class KeystoreArgs {
        @Option(names = {"-k","--keystorepath"}, required = true, description = "Path to the service provider p12 keystore")
        private Path keystorePath;

        @Option(names = {"-s","--keystorepassword"}, required = true,  description = "The password of the service provider keystore")
        String keystorePassword;

        @Option(names = {"-t","--truststorepath"}, required = true, description = "The path of the jks trust store")
        Path trustStorePath;

        @Option(names = {"-w","--truststorepassword"}, required = true, description = "The password of the jks trust store")
        String trustStorePassword;

    }

    static class PemArgs {
       @Option(names = {"--cacert"}, required = true, description = "CA certificate to verify interchange against" )
       private Path rootCertPath;

       @Option(names = {"--cert"}, required = true, description = "Client certificate file name")
       private Path certificatePath;

       @Option(names = {"--key"}, required = true, description = "Client private key file name")
       private Path privateKeyPath;

    }

    static class TlsArgs {
        @ArgGroup(exclusive = false, heading = "Arguments for using PEM keys and certificates\n")
        private PemArgs pemArgs;

        @ArgGroup(exclusive = false, heading = "Arguments for using keystores (keystore is p12, truststore jks)\n")
        private KeystoreArgs keystoreArgs;
    }

    @ArgGroup(exclusive = true, heading = "TLS arguments, only needed for commands connecting directly to the message broker (e.g. listen/send on queues and private channels)\n")
    private TlsArgs tlsArgs;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new ServiceProviderClientApplication());
        commandLine.setExecutionExceptionHandler((ex, cmd, parseResult) -> {
            if (ex instanceof NotAuthenticatedException) {
                cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));
                return 1;
            }
            throw ex;
        });
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    public SSLContext createSSLContext() {
        if (tlsArgs == null) {
            throw new InvalidSSLConfig(
                    "This command needs TLS arguments (either --cacert/--cert/--key or " +
                            "--keystorepath/--keystorepassword/--truststorepath/--truststorepassword) to connect to the message broker",
                    null);
        }
        if (tlsArgs.keystoreArgs != null) {
            KeystoreDetails keystoreDetails = new KeystoreDetails(tlsArgs.keystoreArgs.keystorePath.toString(),
                    tlsArgs.keystoreArgs.keystorePassword,
                    KeystoreType.PKCS12);
            KeystoreDetails trustStoreDetails = new KeystoreDetails(tlsArgs.keystoreArgs.trustStorePath.toString(),
                    tlsArgs.keystoreArgs.trustStorePassword, KeystoreType.JKS);
            return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
        } else {
            KeyStore keyStore;
            try {
                keyStore = ClusterKeyGenerator.newKeyStore(
                        user,
                        "",
                        ClusterKeyGenerator.loadCertificateChain(Files.newBufferedReader(tlsArgs.pemArgs.certificatePath)),
                        ClusterKeyGenerator.safeLoadPrivateKey(Files.newBufferedReader(tlsArgs.pemArgs.privateKeyPath))
                );
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                throw new InvalidSSLConfig("Could not load key store",e);
            }
            KeyStore trustStore;
            try {
                trustStore = ClusterKeyGenerator.newTrustStore(
                        ClusterKeyGenerator.loadSingleCertificate(Files.newBufferedReader(tlsArgs.pemArgs.rootCertPath)),
                        new URI(server).getHost()
                );
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                     URISyntaxException e) {
                throw new InvalidSSLConfig("Could not load trust store",e);
            }
            return SSLContextFactory.newSSLContext(keyStore,"",trustStore);
        }

    }

    public ServiceProviderClient createClient() {
        StoredToken token = new TokenStore().loadIfValid();
        if (token == null) {
            throw new NotAuthenticatedException(
                    "Not authenticated, or the access token has expired. Run 'serviceproviderclient login " +
                            "--token-endpoint=<url> --client-id=<id> --client-secret=<secret>' first.");
        }
        return new ServiceProviderClient(server, user, token.getAccessToken());
    }
}
