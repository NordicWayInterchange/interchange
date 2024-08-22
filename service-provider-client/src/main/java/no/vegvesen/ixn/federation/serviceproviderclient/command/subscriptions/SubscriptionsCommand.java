package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(
        name = "subscriptions",
        description = "list, get, add or delete subscriptions for Service Provider",
        subcommands = {
                GetServiceProviderSubscriptions.class,
                GetSubscription.class,
                AddServiceProviderSubscription.class,
                DeleteServiceProviderSubscription.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class SubscriptionsCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;

    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}
