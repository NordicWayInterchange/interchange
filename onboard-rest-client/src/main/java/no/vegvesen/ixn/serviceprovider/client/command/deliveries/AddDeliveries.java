package no.vegvesen.ixn.serviceprovider.client.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.model.AddDeliveriesRequest;
import no.vegvesen.ixn.serviceprovider.model.AddDeliveriesResponse;
import no.vegvesen.ixn.serviceprovider.model.SelectorApi;
import static picocli.CommandLine.*;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add deliveries for service provider",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddDeliveries implements Callable<Integer> {
    @ParentCommand
    DeliveriesCommand parentCommand;

    @ArgGroup(exclusive = true, multiplicity = "1")
    AddDeliveriesOption option;


    @Override
    public Integer call() throws Exception {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();

        if (option.file != null) {
            AddDeliveriesRequest request = mapper.readValue(option.file, AddDeliveriesRequest.class);
            AddDeliveriesResponse response = client.addServiceProviderDeliveries(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        }
        else{
            AddDeliveriesRequest request = new AddDeliveriesRequest(client.getUser(), Set.of(new SelectorApi(option.selector)));
            AddDeliveriesResponse response = client.addServiceProviderDeliveries(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        }
        return 0;
    }

    static class AddDeliveriesOption{
        @Option(names = {"-f", "--filename"}, required = true, description = "The deliveries json file")
        File file;

        @Option(names = {"-s", "--selector"}, required = true, description = "The delivery selector")
        String selector;
    }
}

