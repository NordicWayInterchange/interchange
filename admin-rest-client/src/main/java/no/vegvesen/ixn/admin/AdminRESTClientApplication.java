package no.vegvesen.ixn.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.model.*;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "admindclient", description = "Admin REST client",showAtFileInUsageHelp = true, subcommands = {
        AdminRESTClientApplication.GetAllNeighbours.class,
        AdminRESTClientApplication.GetNeighbourCapabilities.class,
        AdminRESTClientApplication.GetNeighbourSubscriptions.class,
        AdminRESTClientApplication.GetAllServiceProviders.class,
        AdminRESTClientApplication.GetServiceProvider.class,
        AdminRESTClientApplication.GetServiceProviderCapabilities.class,
        AdminRESTClientApplication.GetServiceProviderSubscriptions.class,
        AdminRESTClientApplication.GetServiceProviderDeliveries.class
})

public class AdminRESTClientApplication implements Callable<Integer> {

    @Parameters(index = "0",description = "The admin server address")
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


    @Command(name = "getAllNeighbours",description = "Get all neighbours")
    static class GetAllNeighbours implements Callable<Integer> {

        @ParentCommand
        AdminRESTClientApplication parentCommand;


        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            GetAllNeighboursResponse allNeighbours = client.getAllNeighbours();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(allNeighbours));
            return 0;
        }
    }

    @Command(name = "getNeighbourCapabilities",description = "Get the neighbour capabilities")
    static class GetNeighbourCapabilities implements Callable<Integer> {

        @ParentCommand
        AdminRESTClientApplication parentCommand;

        @Parameters(index = "0", description = "Name of neighbour")
        String name;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ListNeighbourCapabilitiesResponse neighbourCapabilities = client.getNeighbourCapabilities(name);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(neighbourCapabilities));
            return 0;
        }
    }


    @Command(name = "getNeighbourSubscriptions",description = "Get the neighbour subscriptions")
    static class GetNeighbourSubscriptions implements Callable<Integer> {

        @ParentCommand
        AdminRESTClientApplication parentCommand;

        @Parameters(index = "0", description = "Name of neighbour")
        String name;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ListNeighbourSubscriptionResponse neighbourSubscriptions = client.getNeighbourSubscriptions(name);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(neighbourSubscriptions));
            return 0;
        }
    }

    @Command(name = "getAllServiceProviders",description = "Get all service providers")
    static class GetAllServiceProviders implements Callable<Integer> {

        @ParentCommand
        AdminRESTClientApplication parentCommand;


        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            GetAllServiceProvidersResponse allServiceProviders = client.getAllServiceProviders();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(allServiceProviders));
            return 0;
        }
    }

    @Command(name = "getServiceProvider",description = "Get a service provider")
    static class GetServiceProvider implements Callable<Integer> {

        @ParentCommand
        AdminRESTClientApplication parentCommand;

        @Parameters(index = "0", description = "Name of service provider")
        String name;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            GetServiceProviderResponse serviceProvider = client.getServiceProvider(name);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(serviceProvider));
            return 0;
        }
    }


    @Command(name = "getServiceProvicerCapabilities",description = "Get the service provider capabilities")
    static class GetServiceProviderCapabilities implements Callable<Integer> {

        @ParentCommand
        AdminRESTClientApplication parentCommand;

        @Parameters(index = "0", description = "Name of service provider")
        String name;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ListCapabilitiesResponse serviceProviderCapabilities = client.getServiceProviderCapabilities(name);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(serviceProviderCapabilities));
            return 0;
        }
    }

    @Command(name = "getServiceProvicerSubscriptions", description = "Get the service provider subscriptions")
    static class GetServiceProviderSubscriptions implements Callable<Integer> {

        @ParentCommand
        AdminRESTClientApplication parentCommand;

        @Parameters(index = "0", description = "Name of service provider")
        String name;

        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ListSubscriptionsResponse subscriptions = client.getServiceProviderSubscriptions(name);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(subscriptions));
            return 0;
        }
    }

    @Command(name = "getServiceProvicerDeliveries", description = "List deliveries for service provider")
    static class GetServiceProviderDeliveries implements Callable<Integer> {
        @ParentCommand
        AdminRESTClientApplication parentCommand;

        @Parameters(index = "0", description = "Name of service provider")
        String name;


        @Override
        public Integer call() throws Exception {
            AdminRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            ListDeliveriesResponse response = client.listServiceProviderDeliveries(name);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AdminRESTClientApplication()).execute(args);
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
        return new AdminRESTClient(createSSLContext(),server, user);
    }
}
