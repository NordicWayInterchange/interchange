package no.vegvesen.ixn.napcore.client.command.subscriptions;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "delete",
        description = "Delete nap capability",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeleteNapSubscription implements Callable<Integer> {

    @CommandLine.ParentCommand
    SubscriptionsCommand parentCommand;

    @CommandLine.Parameters(index = "0", description = "The ID of the NAP capability")
    String subscriptionId;

    @Override
    public Integer call() {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        client.deleteSubscription(subscriptionId);
        System.out.printf("NAP subscription with id %s deleted successfully%n", subscriptionId);
        return 0;
    }
}
