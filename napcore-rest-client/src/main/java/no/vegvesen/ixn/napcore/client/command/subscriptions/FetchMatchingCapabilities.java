package no.vegvesen.ixn.napcore.client.command.subscriptions;


import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Capability;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "match",
        description = "Fetch NAP capabilities matching selector",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class FetchMatchingCapabilities implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Parameters(index = "0", description = "The selector to match with the capabilities")
    String selector;

    @Override
    public Integer call() throws JacksonException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = JsonMapper.builder().build();
        List<Capability> capabilities = client.getMatchingCapabilities(selector);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
        return 0;
    }
}
