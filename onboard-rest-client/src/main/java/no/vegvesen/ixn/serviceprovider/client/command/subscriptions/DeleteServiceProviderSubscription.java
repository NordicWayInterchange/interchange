package no.vegvesen.ixn.serviceprovider.client.command.subscriptions;

import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete a service provider subscription",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeleteServiceProviderSubscription implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the subscription to delete")
    String subscriptionId;

    @Override
    public Integer call() {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        client.deleteSubscriptions(subscriptionId);
        System.out.printf("Subscription %s deleted successfully%n", subscriptionId);
        return 0;
    }
}
