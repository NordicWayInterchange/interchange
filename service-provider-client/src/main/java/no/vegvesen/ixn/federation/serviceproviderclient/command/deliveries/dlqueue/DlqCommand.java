package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries.dlqueue;

import no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries.DeliveriesCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "dlq",
        description = "Manage dlq",
        subcommands = {Listen.class},
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class DlqCommand {

    @CommandLine.ParentCommand
    DeliveriesCommand parentCommand;

   public DeliveriesCommand getParent() {
       return parentCommand;
   }
}
