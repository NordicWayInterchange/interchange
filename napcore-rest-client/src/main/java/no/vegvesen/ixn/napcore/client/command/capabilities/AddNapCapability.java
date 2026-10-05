package no.vegvesen.ixn.napcore.client.command.capabilities;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.CapabilitiesRequest;
import no.vegvesen.ixn.napcore.model.OnboardingCapability;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "add",
        description = "Add NAP capability from file",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddNapCapability implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Option(names = {"-f", "--filename"}, description = "The capability json file")
    File file;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = JsonMapper.builder().build();
        CapabilitiesRequest request = mapper.readValue(file, CapabilitiesRequest.class);
        OnboardingCapability response = client.addCapability(request);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
