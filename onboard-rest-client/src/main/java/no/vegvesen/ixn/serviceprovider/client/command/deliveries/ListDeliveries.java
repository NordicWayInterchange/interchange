package no.vegvesen.ixn.serviceprovider.client.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.model.ListDeliveriesResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "List deliveries for service provider",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class ListDeliveries implements Callable<Integer> {
    @ParentCommand
    DeliveriesCommand parentCommand;


    @Override
    public Integer call() throws Exception {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        ListDeliveriesResponse response = client.listServiceProviderDeliveries();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
