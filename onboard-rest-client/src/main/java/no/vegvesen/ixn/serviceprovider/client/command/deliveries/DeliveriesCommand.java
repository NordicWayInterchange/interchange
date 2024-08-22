package no.vegvesen.ixn.serviceprovider.client.command.deliveries;

import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(
        name = "deliveries",
        description = "Get, add list or delete deliveries for a Service Provider",
        subcommands = {
                ListDeliveries.class,
                GetDelivery.class,
                AddDeliveries.class,
                DeleteDelivery.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeliveriesCommand {

    @ParentCommand
    OnboardRestClientApplication parent;


    public OnboardRestClientApplication getParent() {
        return parent;
    }
}
