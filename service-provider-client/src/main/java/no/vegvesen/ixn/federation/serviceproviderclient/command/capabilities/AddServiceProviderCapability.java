package no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.AddCapabilitiesRequest;
import no.vegvesen.ixn.serviceprovider.model.AddCapabilitiesResponse;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "add",
        description = "Add service provider capability from file",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddServiceProviderCapability implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Option(names = {"-f", "--filename"}, description = "The capability json file")
    File file;

    @Override
    public Integer call() throws IOException {
        ServiceProviderClient client = parentCommand.getParentCommand().createClient();

        ObjectMapper mapper = new ObjectMapper();
        AddCapabilitiesRequest capability = mapper.readValue(file, AddCapabilitiesRequest.class);
        AddCapabilitiesResponse result = client.addCapability(capability);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
