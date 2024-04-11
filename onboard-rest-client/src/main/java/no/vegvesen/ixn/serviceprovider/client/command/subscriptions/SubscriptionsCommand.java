package no.vegvesen.ixn.serviceprovider.client.command.subscriptions;

import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
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
    OnboardRestClientApplication parent;

    public OnboardRestClientApplication getParent() {
        return parent;
    }
}
