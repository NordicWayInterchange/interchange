package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.model.GetDeliveryResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(
        name = "get",
        description = "Get a single delivery",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetDelivery implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the delivery to get")
    String deliveryId;

    @Override
    public Integer call() throws Exception {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        GetDeliveryResponse response = client.getDelivery(deliveryId);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}