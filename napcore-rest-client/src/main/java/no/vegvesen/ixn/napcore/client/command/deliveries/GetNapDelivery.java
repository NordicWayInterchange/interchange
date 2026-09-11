package no.vegvesen.ixn.napcore.client.command.deliveries;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Delivery;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

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
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        Delivery delivery = client.getDelivery(deliveryId);
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(delivery));
        return 0;
    }

}
