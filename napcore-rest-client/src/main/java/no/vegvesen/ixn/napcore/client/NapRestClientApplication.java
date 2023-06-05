package no.vegvesen.ixn.napcore.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.model.Capability;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
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
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "napcoreclient",
description = "NAPCore REST Client",
showAtFileInUsageHelp = true,
defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
subcommands = {
        NapRestClientApplication.AddNapSubscription.class,
        NapRestClientApplication.GetNapSubscriptions.class,
        NapRestClientApplication.GetNapSubscription.class,
        NapRestClientApplication.DeleteNapSubscription.class,
        NapRestClientApplication.FetchMatchingCapabilities.class
})
public class NapRestClientApplication implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "SERVER", description = "The NAP server address")
    private String server;

    @Parameters(index = "1", paramLabel = "USER",description = "The service provider user")
    private String user;

    @Option(names = {"-k","--keystorepath"}, required = true, description = "Path to the service provider p12 keystore")
    private Path keystorePath;

    @Option(names = {"-s","--keystorepassword"}, required = true,  description = "The password of the service provider keystore")
    String keystorePassword;

    @Option(names = {"-t","--truststorepath"}, required = true, description = "The path of the jks trust store")
    Path trustStorePath;

    @Option(names = {"-w","--truststorepassword"}, required = true, description = "The password of the jks trust store")
    String trustStorePassword;

    @Command(name = "addsubscription", description = "Add a NAP subscription")
    static class AddNapSubscription implements Callable<Integer> {

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Option(names = {"-f", "--filename"}, description = "The NAP subscription json file")
        File file;

        @Override
        public Integer call() throws IOException {
            NapRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            SubscriptionRequest request = mapper.readValue(file, SubscriptionRequest.class);
            Subscription result = client.addSubscription(request);
            System.out.println(mapper.writeValueAsString(result));
            return 0;
        }
    }

    @Command(name = "getsubscriptions", description = "Get the NAP subscriptions for the service provider")
    static class GetNapSubscriptions implements Callable<Integer> {

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            NapRESTClient client = parentCommand.createClient();
            List<Subscription> subscriptions = client.getSubscriptions();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(subscriptions));
            return 0;
        }
    }

    @Command(name = "getsubscription", description = "Get one NAP subscription for the service provider")
    static class GetNapSubscription implements Callable<Integer> {

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the NAP subscription")
        String subscriptionId;

        @Override
        public Integer call() throws Exception {
            NapRESTClient client = parentCommand.createClient();
            Subscription subscription = client.getSubscription(subscriptionId);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(subscription));
            return 0;
        }
    }

    @Command(name = "deletesubscription", description = "Delete a NAP subscription")
    static class DeleteNapSubscription implements Callable<Integer> {

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the NAP subscription")
        String subscriptionId;

        @Override
        public Integer call() {
            NapRESTClient client = parentCommand.createClient();
            client.deleteSubscription(subscriptionId);
            System.out.printf("NAP subscription with id %s deleted successfully%n", subscriptionId);
            return 0;
        }
    }

    @Command(name = "fetchmatchingcapabilities", description = "Fetch all capabilities in the network matching a selector")
    static class FetchMatchingCapabilities implements Callable<Integer> {

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The selector to match with the capabilities")
        String selector;

        @Override
        public Integer call() throws JsonProcessingException {
            NapRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            List<Capability> matchingCapabilities = client.getMatchingCapabilities(selector);
            System.out.println(mapper.writeValueAsString(matchingCapabilities));
            return 0;
        }
    }

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
        return new NapRESTClient(createSSLContext(), server, user);
    }
}
