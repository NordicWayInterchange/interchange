package no.vegvesen.ixn.napcore.client.command.deliveries;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "delete",
        description = "Delete nap delivery",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeleteNapDelivery implements Callable<Integer> {

    @CommandLine.ParentCommand
    DeliveriesCommand parentCommand;

    @CommandLine.Parameters(index = "0", description = "the ID of the NAP delivery")
    String deliveryId;

    @Override
    public Integer call(){
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        client.deleteDelivery(deliveryId);
        System.out.printf("Nap delivery with id %s deleted successfully", deliveryId);
        return 0;
    }
}
