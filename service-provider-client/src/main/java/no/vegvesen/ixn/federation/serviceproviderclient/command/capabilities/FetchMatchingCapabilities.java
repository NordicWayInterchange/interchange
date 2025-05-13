package no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.FetchMatchingCapabilitiesResponse;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(name = "match", description = "Fetch all capabilities in the network matching a selector",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
        """
                Examples: \n
                serviceproviderclient capabilities match "originatingCountry='NO'" | returns all capabilities matching originatingCountry NO \n
                serviceproviderclient capabilities match | returns all capabilities
                """
})
public class FetchMatchingCapabilities implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Parameters(index = "0", description = "The selector to match with the capabilities")
    String selector;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        System.out.println(String.format("using selector: %s", selector));
        ObjectMapper mapper = new ObjectMapper();
        FetchMatchingCapabilitiesResponse result = client.fetchAllMatchingCapabilities(selector);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
