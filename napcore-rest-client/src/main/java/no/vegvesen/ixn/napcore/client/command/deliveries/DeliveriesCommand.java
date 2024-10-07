package no.vegvesen.ixn.napcore.client.command.deliveries;

import no.vegvesen.ixn.napcore.client.NapRestClientApplication;
import static picocli.CommandLine.*;

@Command(
        name="deliveries",
        description = "Get, add, list or delete NAP deliveries",
        subcommands = {
                AddNapDelivery.class,
                GetNapDelivery.class,
                GetNapDeliveries.class,
                DeleteNapDelivery.class,
                FetchMatchingDeliveryCapabilities.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true)
public class DeliveriesCommand {

    @ParentCommand
    NapRestClientApplication parentCommand;

    public NapRestClientApplication getParentCommand(){
        return parentCommand;
    }

}
