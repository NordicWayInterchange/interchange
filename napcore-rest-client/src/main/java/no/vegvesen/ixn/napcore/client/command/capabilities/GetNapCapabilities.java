package no.vegvesen.ixn.napcore.client.command.capabilities;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.OnboardingCapability;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "list",
        description = "List all NAP capabilities",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetNapCapabilities implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        List<OnboardingCapability> capabilities = client.getCapabilities();
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
        return 0;
    }
}
