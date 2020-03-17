package no.vegvesen.ixn.serviceprovider.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
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

@Command(name = "onboardclient", description = "Onboard REST client",showAtFileInUsageHelp = true, subcommands =
        {OnboardRestClientApplication.GetServiceProviderCapabilities.class,
        OnboardRestClientApplication.AddServerProviderCapabilities.class})
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

    @Command(name = "getcapabilities",description = "Get the serivce provider capabilities")
    static class GetServiceProviderCapabilities implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            OnboardRESTClient client = parentCommand.createClient();
            CapabilityApi serviceProviderCapabilities = client.getServiceProviderCapabilities();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(serviceProviderCapabilities));
            return 0;
        }
    }

    @Command(name = "addcapabilities",description = "Add service provider capabiltites form file")
    static class AddServerProviderCapabilities implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Option(names = {"-f","--filename"}, description = "The capabilities json file")
        File file;

        @Override
        public Integer call() throws IOException {
            OnboardRESTClient client = parentCommand.createClient();
            ObjectMapper mapper = new ObjectMapper();
            CapabilityApi capabilityApi = mapper.readValue(file, CapabilityApi.class);
            CapabilityApi result = client.addCapabilities(capabilityApi);
            System.out.println(mapper.writeValueAsString(result));
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new OnboardRestClientApplication()).execute(args);
        System.exit(exitCode);
        /*
        OnboardRESTClient client = new OnboardRESTClient(sslContext,server,user);
        Object result = null;
        if (args.length > 8) {
            String action = args[7];
            if (action.equals("addsubscription")) {
                result = client.addSubscription(user, new Datex2DataTypeApi("NO"));

            } else {
                System.out.println(String.format("Action %s not recognised",action));
            }
        } else {
            result = client.getServiceProviderCapabilities();
        }
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(result));
*/
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

    private OnboardRESTClient createClient() {
        return new OnboardRESTClient(createSSLContext(),server,user);
    }
}
