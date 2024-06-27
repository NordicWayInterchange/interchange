package no.vegvesen.ixn.napcore.client.command.subscriptions;


import no.vegvesen.ixn.napcore.client.NapRestClientApplication;
import picocli.CommandLine;

@CommandLine.Command(name="subscriptions",
                    description = "List, add or delete NAP subscriptions",
                    subcommands = {
                            AddNapSubscription.class,
                            GetNapSubscription.class,
                            GetNapSubscriptions.class,
                            FetchMatchingCapabilities.class,
                            DeleteNapSubscription.class
                    },
                    defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
                    mixinStandardHelpOptions = true
)
public class SubscriptionsCommand {

    @CommandLine.ParentCommand
    NapRestClientApplication parentCommand;

    public NapRestClientApplication getParentCommand() {
        return parentCommand;
    }
}
