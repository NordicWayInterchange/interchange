package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.AddDeliveriesRequest;
import no.vegvesen.ixn.serviceprovider.model.AddDeliveriesResponse;
import no.vegvesen.ixn.serviceprovider.model.AddDelivery;
import picocli.CommandLine.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add deliveries for service provider",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
        """
                Examples: \n
                serviceproviderclient deliveries add -s "originatingCountry='NO'" -d "Description to delivery" -dlq \n
                serviceproviderclient deliveries add -f denmDelivery.json \n
                -d is optional
                -dlq is optional
                """
}
)
public class AddDeliveries implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @ArgGroup(exclusive = true, multiplicity = "1")
    AddDeliveriesOption option;

    @Option(names = {"-d", "--description"})
    String description;

    @Option(names = {"-dlq", "--dead-letter-queue"}, description = "Messages that couldn't be delivered are moved to dlqueue")
    Boolean dlqueue = false;


    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = JsonMapper.builder().build();

        if (option.file != null) {
            AddDeliveriesRequest request = mapper.readValue(option.file, AddDeliveriesRequest.class);
            AddDeliveriesResponse response = client.addDeliveries(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        }
        else {
            AddDeliveriesRequest request = new AddDeliveriesRequest(client.getUser(), Set.of(new AddDelivery(option.selector, description, dlqueue)));
            AddDeliveriesResponse response = client.addDeliveries(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        }
        return 0;
    }

    private static class AddDeliveriesOption {
        @Option(names = {"-f", "--filename"}, required = true, description = "The deliveries json file")
        File file;

        @Option(names = {"-s", "--selector"}, required = true, description = "The delivery selector")
        String selector;
    }
}
