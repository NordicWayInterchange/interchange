package no.vegvesen.ixn.napcore.client.command.subscriptions;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Subscription;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "get",
        description = "Get one NAP subscription",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetNapSubscription implements Callable<Integer> {

    @CommandLine.ParentCommand
    SubscriptionsCommand parentCommand;

    @CommandLine.Parameters(index = "0", description = "The ID of the NAP capability")
    String subscriptionId;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        Subscription subscription = client.getSubscription(subscriptionId);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
        return 0;
    }

}
