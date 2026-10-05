package no.vegvesen.ixn.napcore.client.command.subscriptions;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Subscription;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "list",
        description = "List all NAP subscriptions",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetNapSubscriptions implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = JsonMapper.builder().build();
        List<Subscription> subscriptions = client.getSubscriptions();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptions));
        return 0;
    }
}
