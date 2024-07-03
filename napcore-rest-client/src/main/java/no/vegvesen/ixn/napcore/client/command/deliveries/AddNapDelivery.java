package no.vegvesen.ixn.napcore.client.command.deliveries;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Delivery;
import no.vegvesen.ixn.napcore.model.DeliveryRequest;
import static picocli.CommandLine.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add NAP delivery from file",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)

public class AddNapDelivery implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @ArgGroup(exclusive = true, multiplicity = "1")
    AddNapDeliveryOption option;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = new ObjectMapper();
        if(option.file != null) {
            DeliveryRequest request = mapper.readValue(option.file, DeliveryRequest.class);
            Delivery response = client.addDelivery(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        }
        else {
            DeliveryRequest request = new DeliveryRequest(option.selector);
            Delivery response = client.addDelivery(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        }
        return 0;
    }
    static class AddNapDeliveryOption{
        @Option(names = {"-f", "--filename"}, required = true, description = "The subscription json file")
        File file;

        @Option(names = {"-s", "--selector"}, required = true, description = "The subscription selector")
        String selector;
    }
}

