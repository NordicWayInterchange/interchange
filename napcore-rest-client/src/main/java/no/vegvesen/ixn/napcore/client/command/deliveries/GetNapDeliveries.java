package no.vegvesen.ixn.napcore.client.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Delivery;
import static picocli.CommandLine.*;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "List all nap deliveries",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetNapDeliveries implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;


    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = new ObjectMapper();
        List<Delivery> deliveries = client.getDeliveries();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deliveries));
        return 0;
    }
}
