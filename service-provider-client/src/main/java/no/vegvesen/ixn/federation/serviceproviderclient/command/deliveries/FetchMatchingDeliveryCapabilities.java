package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.FetchMatchingCapabilitiesResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.Callable;

@Command(name = "match", description = "Fetch all local capabilities matching a selector",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
        """
                Examples: \n
                serviceproviderclient deliveries match "originatingCountry='NO'" # returns all local capabilities matching originatingCountry NO \n
                serviceproviderclient deliveries match "" # returns all local capabilities
                """
})
public class FetchMatchingDeliveryCapabilities implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @Parameters(index = "0", description = "The selector to match with the capabilities")
    String selector;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        System.out.println(String.format("using selector: %s", selector));
        ObjectMapper mapper = JsonMapper.builder().build();
        FetchMatchingCapabilitiesResponse result = client.fetchMatchingDeliveryCapabilitiesResponse(selector);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
