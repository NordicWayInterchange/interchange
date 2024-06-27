package no.vegvesen.ixn.napcore.client.command.deliveries;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Delivery;
import no.vegvesen.ixn.napcore.model.DeliveryRequest;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "add",
        description = "Add NAP delivery from file",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)

public class AddNapDelivery implements Callable<Integer> {

    @CommandLine.ParentCommand
    DeliveriesCommand parentCommand;

    @CommandLine.Option(names = {"-f", "--filename"}, description = "The Nap delivery json file")
    File file;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = new ObjectMapper();
        DeliveryRequest request = mapper.readValue(file, DeliveryRequest.class);
        Delivery response = client.addDelivery(request);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }

}
