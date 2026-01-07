package no.vegvesen.ixn.federation.serviceproviderclient.command.biqueue;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import picocli.CommandLine;

@CommandLine.Command(
        name = "bi-queue",
        description = "Manage bi-queue",
        subcommands = {
                Listen.class,
                ListBiqueues.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class BiqueueCommand {

    @CommandLine.ParentCommand
    ServiceProviderClientApplication parent;

    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}
