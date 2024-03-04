package no.vegvesen.ixn.serviceprovider.client.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.model.AddCapabilitiesRequest;
import no.vegvesen.ixn.serviceprovider.model.AddCapabilitiesResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add service provider capability from file",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddServiceProviderCapability implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Option(names = {"-f", "--filename"}, description = "The capability json file")
    File file;

    @Override
    public Integer call() throws IOException {
        OnboardRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = new ObjectMapper();
        AddCapabilitiesRequest capability = mapper.readValue(file, AddCapabilitiesRequest.class);
        AddCapabilitiesResponse result = client.addCapability(capability);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
