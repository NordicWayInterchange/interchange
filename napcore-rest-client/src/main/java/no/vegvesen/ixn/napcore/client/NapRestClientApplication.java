package no.vegvesen.ixn.napcore.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        NapRestClientApplication.AddNapSubscription.class,
        NapRestClientApplication.GetNapSubscriptions.class,
        NapRestClientApplication.GetNapSubscription.class,
        NapRestClientApplication.DeleteNapSubscription.class,
        NapRestClientApplication.FetchMatchingCapabilities.class,
        NapRestClientApplication.CreateKeys.class,
        NapRestClientApplication.AddNapDelivery.class,
        NapRestClientApplication.GetNapDelivery.class,
        NapRestClientApplication.GetNapDeliveries.class,
        NapRestClientApplication.DeleteNapDelivery.class,
        NapRestClientApplication.FetchMatchingDeliveryCapabilities.class,
        NapRestClientApplication.AddNapCapability.class,
        NapRestClientApplication.GetNapCapabilities.class,
        NapRestClientApplication.DeleteNapCapability.class,
        NapRestClientApplication.GetNapCapability.class
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
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
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
            ObjectMapper mapper = new ObjectMapper();
            List<Subscription> subscriptions = client.getSubscriptions();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptions));
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
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
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
            List<Capability> capabilities = client.getMatchingCapabilities(selector);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
            return 0;
        }
    }

    @Command(name = "requestcertificate", description = "Create a certificate for NAP user")
    static class CreateKeys implements Callable<Integer> {
        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", paramLabel = "SERVICE_PROVIDER_NAME",description = "Service Provider name")
        String spName;

        @Parameters(index = "1", paramLabel = "SERVICE_PROVIDER_COUNTRY",description = "Service Provider country code")
        String countryCode;


        @Override
        public Integer call() throws Exception {
            NapRESTClient client = parentCommand.createClient();
            NapRESTClient.KeyAndCSR keyAndCSR = client.generateKeyAndCSR(spName, countryCode);
            CertificateSignResponse certificateSignResponse = client.requestCertificate(new CertificateSignRequest(Base64.getEncoder().encodeToString(keyAndCSR.getCsr().getBytes())));
            System.out.println(keyAndCSR.getKey());
            List<String> decodedChain = certificateSignResponse.getChain().stream().map(s -> new String(Base64.getDecoder().decode(s.getBytes()))).collect(Collectors.toList());
            System.out.println(String.join("",decodedChain));
            return 0;
        }
    }

    @Command(name="adddelivery", description = "Add a NAP delivery")
    static class AddNapDelivery implements Callable<Integer>{

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Option(names = {"-f", "--filename"}, description = "The Nap delivery json file")
        File file;

        @Override
        public Integer call() throws IOException{
            NapRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            DeliveryRequest request = mapper.readValue(file, DeliveryRequest.class);
            Delivery response = client.addDelivery(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            return 0;
        }
    }

    @Command(name="getdelivery", description = "Get one NAP delivery for the service provider")
    static class GetNapDelivery implements Callable<Integer>{

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the NAP delivery")
        String deliveryId;

        @Override
        public Integer call() throws Exception{
            NapRESTClient client = parentCommand.createClient();
            Delivery delivery = client.getDelivery(deliveryId);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(delivery));
            return 0;
        }
    }

    @Command(name="getdeliveries", description = "Get the NAP deliveries for the service provider")
    static class GetNapDeliveries implements Callable<Integer>{

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            NapRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            List<Delivery> deliveries = client.getDeliveries();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deliveries));
            return 0;
        }
    }

    @Command(name="deletedelivery", description = "Delete a NAP delivery")
    static class DeleteNapDelivery implements Callable<Integer> {

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", description = "the ID of the NAP delivery")
        String deliveryId;

        @Override
        public Integer call(){
            NapRESTClient client = parentCommand.createClient();
            client.deleteDelivery(deliveryId);
            System.out.printf("Nap delivery with id %s deleted successfully", deliveryId);
            return 0;
        }
    }

    @Command(name="fetchmatchingdeliverycapabilities", description = "Get service providers capabilities matching selector")
    static class FetchMatchingDeliveryCapabilities implements Callable<Integer>{

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The selector to match with the capabilities")
        String selector;

        @Override
        public Integer call() throws JsonProcessingException {
            NapRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            List<Capability> capabilities = client.getMatchingDeliveryCapabilities(selector);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
            return 0;
        }
    }

    @Command(name="addcapability", description = "Add a NAP capability")
    static class AddNapCapability implements Callable<Integer> {

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Option(names = {"-f", "--filename"}, description = "The NAP capability json file")
        File file;

        @Override
        public Integer call() throws IOException {
            NapRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            CapabilitiesRequest request = mapper.readValue(file, CapabilitiesRequest.class);
            OnboardingCapability response = client.addCapability(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            return 0;
        }
    }

    @Command(name="getcapability", description = "Get one NAP capability for the service provider")
    static class GetNapCapability implements Callable<Integer> {

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the NAP capability")
        String capabilityId;

        @Override
        public Integer call() throws JsonProcessingException {
            NapRESTClient client = parentCommand.createClient();
            OnboardingCapability capability = client.getCapability(capabilityId);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capability));
            return 0;
        }
    }

    @Command(name = "getcapabilities", description = "Get the NAP capabilities for the service provider")
    static class GetNapCapabilities implements Callable<Integer>{

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Override
        public Integer call() throws JsonProcessingException {
            NapRESTClient client = parentCommand.createClient();
            List<OnboardingCapability> capabilities = client.getCapabilities();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
            return 0;
        }
    }

    @Command(name="deletecapability", description = "Delete a NAP capability")
    static class DeleteNapCapability implements Callable<Integer>{

        @ParentCommand
        NapRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the NAP capability")
        String capabilityId;

        @Override
        public Integer call(){
            NapRESTClient client = parentCommand.createClient();
            client.deleteCapability(capabilityId);
            System.out.printf("Nap capability with id %s deleted successfully", capabilityId);
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
        return new NapRESTClient(createSSLContext(), server, user, nap);
    }
}
