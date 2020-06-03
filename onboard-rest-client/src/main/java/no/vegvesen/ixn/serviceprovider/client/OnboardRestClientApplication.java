package no.vegvesen.ixn.serviceprovider.client;

/*-
 * #%L
 * onboard-rest-client
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.serviceprovider.model.LocalDataType;
import no.vegvesen.ixn.serviceprovider.model.LocalDataTypeList;
import no.vegvesen.ixn.serviceprovider.model.LocalSubscriptionApi;
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
        OnboardRestClientApplication.DeleteServiceProviderSubscription.class
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
            LocalDataTypeList serviceProviderCapabilities = client.getServiceProviderCapabilities();
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
            DataTypeApi capability = mapper.readValue(file,DataTypeApi.class);
            LocalDataType result = client.addCapability(capability);
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
            LocalDataTypeList capabilities = client.getServiceProviderCapabilities();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(capabilities));
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
            DataTypeApi subscription = mapper.readValue(file,DataTypeApi.class);
            LocalSubscriptionApi result = client.addSubscription(subscription);
            System.out.println(mapper.writeValueAsString(result));
            return 0;
        }
    }

    @Command(name = "deletecapability", description = "Delete a service provider capability")
    static class DeleteServiceProviderCapability implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the capability to delete")
        Integer capabilityId;

        @Override
        public Integer call() {
            OnboardRESTClient client = parentCommand.createClient();
            client.deleteCapability(capabilityId);
            System.out.println(String.format("Capability %d deleted successfully",capabilityId));
            return 0;
        }
    }

    @Command(name = "deletesubscription", description = "Delete a service provider subscription")
    static class DeleteServiceProviderSubscription implements Callable<Integer> {

        @ParentCommand
        OnboardRestClientApplication parentCommand;

        @Parameters(index = "0", description = "The ID of the subscription to delete")
        Integer subscriptionId;

        @Override
        public Integer call() throws Exception {
            OnboardRESTClient client = parentCommand.createClient();
            client.deleteSubscriptions(subscriptionId);
            System.out.println(String.format("Subscription %d deleted successfully",subscriptionId));
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
                KeystoreType.PKCS12, keyPassword);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }

    public OnboardRESTClient createClient() {
        return new OnboardRESTClient(createSSLContext(),server,user);
    }
}
