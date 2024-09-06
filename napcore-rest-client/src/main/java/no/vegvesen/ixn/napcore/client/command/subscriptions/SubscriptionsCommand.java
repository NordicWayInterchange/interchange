package no.vegvesen.ixn.napcore.client.command.subscriptions;


import no.vegvesen.ixn.napcore.client.NapRestClientApplication;
import static picocli.CommandLine.*;

@Command(name="subscriptions",
                    description = "List, add or delete NAP subscriptions",
                    subcommands = {
                            AddNapSubscription.class,
                            GetNapSubscription.class,
                            GetNapSubscriptions.class,
                            FetchMatchingCapabilities.class,
                            DeleteNapSubscription.class
                    },
                    defaultValueProvider = PropertiesDefaultProvider.class,
                    mixinStandardHelpOptions = true
)
public class SubscriptionsCommand {

    @ParentCommand
    NapRestClientApplication parentCommand;

    public NapRestClientApplication getParentCommand() {
        return parentCommand;
    }
}
