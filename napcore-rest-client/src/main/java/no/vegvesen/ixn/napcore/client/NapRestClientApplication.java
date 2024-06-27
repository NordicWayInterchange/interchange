package no.vegvesen.ixn.napcore.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.command.capabilities.CapabilitiesCommand;
import no.vegvesen.ixn.napcore.client.command.deliveries.DeliveriesCommand;
import no.vegvesen.ixn.napcore.client.command.keys.KeysCommand;
import no.vegvesen.ixn.napcore.client.command.subscriptions.SubscriptionsCommand;
import no.vegvesen.ixn.napcore.model.*;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;


import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "napcoreclient",
description = "NAPCore REST Client",
showAtFileInUsageHelp = true,
defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
subcommands = {
        CapabilitiesCommand.class,
        DeliveriesCommand.class,
        KeysCommand.class,
        SubscriptionsCommand.class
})
public class NapRestClientApplication implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "SERVER", description = "The NAP server address")
    private String server;

    @Parameters(index = "1", paramLabel = "USER", description = "The service provider user")
    private String user;

    @Parameters(index = "2", paramLabel = "NAP", description = "The name of the NAP")
    private String nap;

    @Option(names = {"-k","--keystorepath"}, required = true, description = "Path to the service provider p12 keystore")
    private Path keystorePath;

    @Option(names = {"-s","--keystorepassword"}, required = true,  description = "The password of the service provider keystore")
    String keystorePassword;

    @Option(names = {"-t","--truststorepath"}, required = true, description = "The path of the jks trust store")
    Path trustStorePath;

    @Option(names = {"-w","--truststorepassword"}, required = true, description = "The password of the jks trust store")
    String trustStorePassword;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new NapRestClientApplication()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }

    private SSLContext createSSLContext() {
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath.toString(),
                keystorePassword,
                KeystoreType.PKCS12);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath.toString(),
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }

    public NapRESTClient createClient() {
        return new NapRESTClient(createSSLContext(), server, user, nap);
    }
}
