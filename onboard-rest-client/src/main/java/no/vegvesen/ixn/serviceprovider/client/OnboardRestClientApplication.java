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

@Command(name = "onboardclient", description = "Onboard REST client",showAtFileInUsageHelp = true, subcommands = {
        OnboardRestClientApplication.GetServiceProviderCapabilities.class,
        OnboardRestClientApplication.AddServiceProviderCapability.class,
        OnboardRestClientApplication.GetServiceProviderSubscriptions.class,
        OnboardRestClientApplication.AddServiceProviderSubscription.class,
        OnboardRestClientApplication.DeleteServiceProviderCapability.class,
        OnboardRestClientApplication.DeleteServiceProviderSubscription.class,
        OnboardRestClientApplication.AddDeliveries.class,
        OnboardRestClientApplication.ListDeliveries.class,
        OnboardRestClientApplication.GetDelivery.class,
        OnboardRestClientApplication.DeleteDelivery.class,
        OnboardRestClientApplication.GetSubscription.class,
        OnboardRestClientApplication.AddPrivateChannel.class,
        OnboardRestClientApplication.GetPrivateChannels.class,
        OnboardRestClientApplication.DeletePrivateChannel.class,
        OnboardRestClientApplication.FetchMatchingCapabilities.class
})
public class OnboardRestClientApplication implements Callable<Integer> {

    @Parameters(index = "0",description = "The onboard server address")
    private String server;

    @Parameters(index = "1", description = "The service provider user")
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

    @Command(name = "getcapabilities",description = "Get the service provider capabilities")
    static class GetServiceProviderCapabilities implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            OnboardRESTClient client = parentCommand.createClient();
            ListCapabilitiesResponse serviceProviderCapabilities = client.getServiceProviderCapabilities();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(serviceProviderCapabilities));
            return 0;
        }
    }

    @Command(name = "addcapability",description = "Add service provider capability from file")
    static class AddServiceProviderCapability implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Option(names = {"-f","--filename"}, description = "The capability json file")
        File file;

        @Override
        public Integer call() throws IOException {
            OnboardRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            AddCapabilitiesRequest capability = mapper.readValue(file,AddCapabilitiesRequest.class);
            AddCapabilitiesResponse result = client.addCapability(capability);
            System.out.println(mapper.writeValueAsString(result));
            return 0;
        }
    }

    @Command(name = "getsubscriptions", description = "Get the service provider subscriptions")
    static class GetServiceProviderSubscriptions implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            OnboardRESTClient client = parentCommand.createClient();
            ListSubscriptionsResponse subscriptions = client.getServiceProviderSubscriptions();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptions));
            return 0;
        }
    }

    @Command(name = "addsubscription", description = "Add a subscription for the service provider")
    static class AddServiceProviderSubscription implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Option(names = {"-f","--filename"}, description = "The subscription json file")
        File file;

        @Override
        public Integer call() throws Exception {
            OnboardRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            AddSubscriptionsRequest requestApi = mapper.readValue(file, AddSubscriptionsRequest.class);
            AddSubscriptionsResponse result = client.addSubscription(requestApi);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            return 0;
        }
    }

    @Command(name = "deletecapability", description = "Delete a service provider capability")
    static class DeleteServiceProviderCapability implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the capability to delete")
        String capabilityId;

        @Override
        public Integer call() {
            OnboardRESTClient client = parentCommand.createClient();
            client.deleteCapability(capabilityId);
            System.out.printf("Capability %s deleted successfully%n",capabilityId);
            return 0;
        }
    }

    @Command(name = "deletesubscription", description = "Delete a service provider subscription")
    static class DeleteServiceProviderSubscription implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the subscription to delete")
        String subscriptionId;

        @Override
        public Integer call(){
            OnboardRESTClient client = parentCommand.createClient();
            client.deleteSubscriptions(subscriptionId);
            System.out.printf("Subscription %s deleted successfully%n",subscriptionId);
            return 0;
        }
    }

    @Command(name = "getsubscription", description = "Retrieving brokerUrl for remote service provider subscription")
    static class GetSubscription implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the subscription with the brokerUrl")
        Integer subscriptionId;

        @Override
        public Integer call() throws JsonProcessingException {
            OnboardRESTClient client = parentCommand.createClient();
            GetSubscriptionResponse subscription = client.getSubscription(subscriptionId);
            System.out.printf("Subscription %d successfully polled with %n", subscriptionId);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
            return 0;
        }
    }

    @Command(name = "adddeliveries", description = "Add deliveries for service provider")
    static class AddDeliveries implements Callable<Integer> {
        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Option(names = {"-f","--filename"}, description = "The deliveries json file")
        File file;


        @Override
        public Integer call() throws Exception {
            OnboardRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            AddDeliveriesRequest request = mapper.readValue(file,AddDeliveriesRequest.class);
            AddDeliveriesResponse response = client.addServiceProviderDeliveries(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            return 0;
        }
    }

    @Command(name = "listdeliveries", description = "List deliveries for service provider")
    static class ListDeliveries implements Callable<Integer> {
        @ParentCommand
        OnboardRestClientApplication parentCommand;


        @Override
        public Integer call() throws Exception {
            OnboardRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            ListDeliveriesResponse response = client.listServiceProviderDeliveries();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            return 0;
        }
    }

    @Command(name = "getdelivery", description = "Get a single delivery")
    static class GetDelivery implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the delivery to get")
        String deliveryId;

        @Override
        public Integer call() throws Exception {
            OnboardRESTClient  client = parentCommand.createClient();
            GetDeliveryResponse response = client.getDelivery(deliveryId);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            return 0;
        }
    }

    @Command(name = "deletedelivery", description = "Delete a single delivery")
   static class DeleteDelivery implements  Callable<Integer> {

        @ParentCommand
       OnboardRestClientApplication parentCommand;

       @Parameters(index = "0", description = "The ID of the delivery to delete")
       String deliveryId;


        @Override
        public Integer call() throws Exception {
            OnboardRESTClient  client = parentCommand.createClient();
            client.deleteDelivery(deliveryId);
            System.out.println(String.format("Delivery %s has been deleted",deliveryId));
            return 0;
        }
    }

    @Command(name = "addprivatechannel", description = "Adding name for client to set up private channel")
    static class AddPrivateChannel implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Option(names = {"-f","--filename"}, description = "The json file for the peerName")
        File file;

        @Override
        public Integer call() throws IOException {
            OnboardRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            PrivateChannelApi privateChannel = mapper.readValue(file,PrivateChannelApi.class);
            PrivateChannelApi result = client.addPrivateChannel(privateChannel);
            System.out.println(mapper.writeValueAsString(result));
            return 0;
        }
    }

    @Command(name = "deleteprivatechannel", description = "Delete a service provider private channel to a client")
    static class DeletePrivateChannel implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the subscription to delete")
        Integer privateChannelId;

        @Override
        public Integer call(){
            OnboardRESTClient client = parentCommand.createClient();
            client.deletePrivateChannel(privateChannelId);
            System.out.printf("Private channel with id %d deleted successfully%n",privateChannelId);
            return 0;
        }
    }

    @Command(name = "getprivatechannels", description = "Get the serviceProvider private channels to clients")
    static class GetPrivateChannels implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Override
        public Integer call() throws IOException {
            OnboardRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            PrivateChannelListApi result = client.getPrivateChannels();
            System.out.println(mapper.writeValueAsString(result));
            return 0;
        }
    }

    @Command(name = "fetchmatchingcapabilities", description = "Fetch all capabilities in the network matching a selector")
    static class FetchMatchingCapabilities implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The selector to match with the capabilities")
        String selector;

        @Override
        public Integer call() throws Exception {
            OnboardRESTClient client = parentCommand.createClient();
            System.out.println(String.format("using selector: %s", selector));
            ObjectMapper mapper = new ObjectMapper();
            FetchMatchingCapabilitiesResponse result = client.fetchAllMatchingCapabilities(selector);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new OnboardRestClientApplication()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }

    private SSLContext createSSLContext() {
         KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }

    public OnboardRESTClient createClient() {
        return new OnboardRESTClient(createSSLContext(),server,user);
    }
}
