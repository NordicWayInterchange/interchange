package no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import no.vegvesen.ixn.serviceprovider.model.FetchMatchingCapabilitiesResponse;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(name = "match", description = "Fetch all capabilities in the network matching a selector")
public class FetchMatchingCapabilities implements Callable<Integer> {

    @ParentCommand
    ServiceProviderClientApplication parentCommand;

    @Parameters(index = "0", description = "The selector to match with the capabilities")
    String selector;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.createClient();
        System.out.println(String.format("using selector: %s", selector));
        ObjectMapper mapper = new ObjectMapper();
        FetchMatchingCapabilitiesResponse result = client.fetchAllMatchingCapabilities(selector);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
