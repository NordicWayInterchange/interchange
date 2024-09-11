package no.vegvesen.ixn.napcore.client.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Subscription;
import static picocli.CommandLine.*;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.ParentCommand;
import java.util.List;
import java.util.concurrent.Callable;

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
        ObjectMapper mapper = new ObjectMapper();
        List<Subscription> subscriptions = client.getSubscriptions();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptions));
        return 0;
    }
}
