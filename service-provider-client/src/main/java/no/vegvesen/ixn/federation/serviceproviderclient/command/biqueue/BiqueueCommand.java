package no.vegvesen.ixn.federation.serviceproviderclient.command.biqueue;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import picocli.CommandLine;


@CommandLine.Command(
        name = "bi-queue",
        description = "Show, add, or remove service provider's access to bi queue",
        subcommands = {
                GetBiqueueAccessToServiceProvider.class,
                AddBiqueueAccessToServiceProvider.class,
                RemoveBiqueueAccessToServiceProvider.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class BiqueueCommand {

    @CommandLine.ParentCommand
    ServiceProviderClientApplication parentCommand;

    public ServiceProviderClientApplication getParent() {
        return parentCommand;
    }
}

