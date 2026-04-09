package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.GetDeliveryResponse;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "get",
        description = "Get a single delivery",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
        """
                Example: \n
                serviceproviderclient deliveries get a4154ea0-77aa-4f04-9e3f-c325430fd2be
                """
}
)
public class GetDelivery implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the delivery to get")
    String deliveryId;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        GetDeliveryResponse response = client.getDelivery(deliveryId);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}