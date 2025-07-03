package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import picocli.CommandLine.*;

@Command(
        name = "subscriptions",
        description = "list, get, add or delete subscriptions for service provider",
        subcommands = {
                GetSubscriptions.class,
                GetSubscription.class,
                AddSubscriptions.class,
                DeleteSubscription.class,
                Listen.class,
                CountMessages.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class SubscriptionsCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;

    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}
