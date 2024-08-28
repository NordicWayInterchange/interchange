package no.vegvesen.ixn.federation.serviceproviderclient;

import no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities.CapabilitiesCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities.FetchMatchingCapabilities;
import no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries.DeliveriesCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.jms.MessagesCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.PrivateChannelsCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions.SubscriptionsCommand;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;

import static picocli.CommandLine.*;

@Command(name = "serviceproviderclient",
        description = "Service provider client",
        showAtFileInUsageHelp = true,
        defaultValueProvider = PropertiesDefaultProvider.class,
        subcommands = {
                CapabilitiesCommand.class,
                DeliveriesCommand.class,
                SubscriptionsCommand.class,
                PrivateChannelsCommand.class,
                FetchMatchingCapabilities.class,
                MessagesCommand.class
        },
        mixinStandardHelpOptions = true)
public class ServiceProviderClientApplication{

    @Parameters(index = "0", paramLabel = "SERVER", description = "The onboard server address")
    private String server;

    @Option(names = {"-u", "--user"}, required = false, description = "The service provider user")
    private String user;

    @Option(names = {"-k","--keystorepath"}, required = true, description = "Path to the service provider p12 keystore")
    private Path keystorePath;

    @Option(names = {"-s","--keystorepassword"}, required = true,  description = "The password of the service provider keystore")
    String keystorePassword;

    @Option(names = {"-t","--truststorepath"}, required = true, description = "The path of the jks trust store")
    Path trustStorePath;

    @Option(names = {"-w","--truststorepassword"}, required = true, description = "The password of the jks trust store")
    String trustStorePassword;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ServiceProviderClientApplication()).execute(args);
        System.exit(exitCode);
    }

    public String getUrl() {
        return server;
    }

    public Path getKeystorePath() {
        return keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public Path getTrustStorePath() {
        return trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    private SSLContext createSSLContext() {
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath.toString(),
                keystorePassword,
                KeystoreType.PKCS12);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath.toString(),
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }
    public ServiceProviderClient createClient() {
        return new ServiceProviderClient(createSSLContext(),server,user);
    }
}
