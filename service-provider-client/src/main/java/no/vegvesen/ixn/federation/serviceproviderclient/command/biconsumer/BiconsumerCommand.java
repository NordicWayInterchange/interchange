package no.vegvesen.ixn.federation.serviceproviderclient.command.biconsumer;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import picocli.CommandLine;


@CommandLine.Command(
        name = "bi-consumer",
        description = "Show, add, or remove service provider's access to bi-consumer",
        subcommands = {
                GetBiconsumerAccessToServiceProvider.class,
                AddBiconsumerAccessToServiceProvider.class,
                RemoveBiconsumerAccessToServiceProvider.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class BiconsumerCommand {

    @CommandLine.ParentCommand
    ServiceProviderClientApplication parentCommand;

    public ServiceProviderClientApplication getParent() {
        return parentCommand;
    }
}

