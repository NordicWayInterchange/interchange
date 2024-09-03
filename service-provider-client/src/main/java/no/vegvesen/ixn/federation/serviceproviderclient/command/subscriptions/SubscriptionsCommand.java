package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import picocli.CommandLine.*;

@Command(
        name = "subscriptions",
        description = "list, get, add or delete subscriptions for Service Provider",
        subcommands = {
                GetServiceProviderSubscriptions.class,
                GetSubscription.class,
                AddServiceProviderSubscription.class,
                DeleteServiceProviderSubscription.class,
                Listen.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class SubscriptionsCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;

    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}
