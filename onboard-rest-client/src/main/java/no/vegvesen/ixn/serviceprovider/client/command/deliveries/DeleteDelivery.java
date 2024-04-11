package no.vegvesen.ixn.serviceprovider.client.command.deliveries;

import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

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
        OnboardRESTClient client = parentCommand.getParent().createClient();
        client.deleteDelivery(deliveryId);
        System.out.printf("Delivery %s has been deleted%n", deliveryId);
        return 0;
    }
}
