package no.vegvesen.ixn.serviceprovider.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.serviceprovider.model.*;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "adminclient", description = "Admin REST client",showAtFileInUsageHelp = true, subcommands = {
        AdminRestClientApplication.GetNeighbourCapabilities.class,
        AdminRestClientApplication.GetNeihbourSubscriptions.class,
        AdminRestClientApplication.GetServiceProviderCapabilities.class,
        AdminRestClientApplication.GetServiceProviderSubscriptions.class,
        AdminRestClientApplication.ListDeliveries.class,
})
public class AdminRestClientApplication implements Callable<Integer> {

    @Parameters(index = "0",description = "The Admin server address")
    private String server;

    @Parameters(index = "1", description = "The admin user")
    private String user;

    @Option(names = {"-k","--keystorepath"}, description = "Path to the service provider p12 keystore")
    private String keystorePath;

    @Option(names = {"-s","--keystorepassword"}, description = "The password of the service provider keystore")
    String keystorePassword;

    @Option(names = {"-p", "--keypassword"}, description = "The password of the service provider key")
    String keyPassword;

    @Option(names = {"-t","--truststorepath"}, description = "The path of the jks trust store")
    String trustStorePath;

    @Option(names = {"-w","--truststorepassword"}, description = "The password of the jks trust store")
    String trustStorePassword;

    @Command(name = "getNeihbourCapabilities",description = "Get the neighbour capabilities")
    static class GetNeighbourCapabilities implements Callable<Integer> {

        @ParentCommand
        AdminRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ListCapabilitiesResponse neighbourCapabilities = client.getNeighbourCapabilities();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(neighbourCapabilities));
            return 0;
        }
    }

    @Command(name = "getNeihbourSubscriptions",description = "Get the neighbour subscriptions")
    static class GetNeihbourSubscriptions implements Callable<Integer> {

        @ParentCommand
        AdminRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ListCapabilitiesResponse neighbourSubscriptions = client.getNeihbourSubscriptions();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(neighbourSubscriptions));
            return 0;
        }
    }

    @Command(name = "getServiceProviderCapabilities",description = "Get the service provider capabilities")
    static class GetServiceProviderCapabilities implements Callable<Integer> {

        @ParentCommand
        AdminRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ListCapabilitiesResponse serviceProviderCapabilities = client.getServiceProviderCapabilities();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(serviceProviderCapabilities));
            return 0;
        }
    }

    @Command(name = "getServiceProviderSubscriptions", description = "Get the service provider subscriptions")
    static class GetServiceProviderSubscriptions implements Callable<Integer> {

        @ParentCommand
        AdminRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ListSubscriptionsResponse subscriptions = client.getServiceProviderSubscriptions();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(subscriptions));
            return 0;
        }
    }

    @Command(name = "listServiceProviderDeliveries", description = "List deliveries for service provider")
    static class ListDeliveries implements Callable<Integer> {
        @ParentCommand
        AdminRestClientApplication parentCommand;


        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            ListDeliveriesResponse response = client.listServiceProviderDeliveries();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AdminRestClientApplication()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }

    private SSLContext createSSLContext() {
         KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }

    public AdminRESTClient createClient() {
        return new AdminRESTClient(createSSLContext(),server,user);
    }
}
