package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete a single delivery",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeleteDelivery implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the delivery to delete")
    String deliveryId;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        client.deleteDelivery(deliveryId);
        System.out.printf("Delivery %s has been deleted%n", deliveryId);
        return 0;
    }
}
