package no.vegvesen.ixn.napcore.client.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Delivery;
import static picocli.CommandLine.*;
import java.util.concurrent.Callable;

@Command(
        name = "get",
        description = "Get one NAP delivery",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetNapDelivery implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the NAP delivery")
    String deliveryId;

    @Override
    public Integer call() throws Exception{
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        Delivery delivery = client.getDelivery(deliveryId);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(delivery));
        return 0;
    }

}
