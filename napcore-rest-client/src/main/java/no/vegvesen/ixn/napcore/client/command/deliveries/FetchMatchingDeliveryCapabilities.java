package no.vegvesen.ixn.napcore.client.command.deliveries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Capability;
import static picocli.CommandLine.*;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "fetchdeliverycapabilities",
        description = "Fetch NAP capabilities matching selector",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class FetchMatchingDeliveryCapabilities implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @Parameters(index = "0", description = "The selector to match with the capabilities")
    String selector;

    @Override
    public Integer call() throws JsonProcessingException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = new ObjectMapper();
        List<Capability> capabilities = client.getMatchingDeliveryCapabilities(selector);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
        return 0;
    }
}
