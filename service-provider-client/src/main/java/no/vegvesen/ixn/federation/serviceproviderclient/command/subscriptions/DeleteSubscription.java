package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete a service provider subscription",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeleteSubscription implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the subscription to delete")
    String subscriptionId;

    @Override
    public Integer call() {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        client.deleteSubscriptions(subscriptionId);
        System.out.printf("Subscription %s deleted successfully%n", subscriptionId);
        return 0;
    }
}
