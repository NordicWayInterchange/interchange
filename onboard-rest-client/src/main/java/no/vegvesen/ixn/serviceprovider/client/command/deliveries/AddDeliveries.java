package no.vegvesen.ixn.serviceprovider.client.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import no.vegvesen.ixn.serviceprovider.model.AddDeliveriesRequest;
import no.vegvesen.ixn.serviceprovider.model.AddDeliveriesResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add deliveries for service provider",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddDeliveries implements Callable<Integer> {
    @ParentCommand
    DeliveriesCommand parentCommand;

    @Option(names = {"-f", "--filename"}, description = "The deliveries json file")
    File file;


    @Override
    public Integer call() throws Exception {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        AddDeliveriesRequest request = mapper.readValue(file, AddDeliveriesRequest.class);
        AddDeliveriesResponse response = client.addServiceProviderDeliveries(request);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
