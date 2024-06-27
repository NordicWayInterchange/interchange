package no.vegvesen.ixn.napcore.client.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Delivery;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "get",
        description = "Get one NAP delivery",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetNapDelivery implements Callable<Integer> {

    @CommandLine.ParentCommand
    DeliveriesCommand parentCommand;

    @CommandLine.Parameters(index = "0", description = "The ID of the NAP delivery")
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
