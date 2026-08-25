package no.vegvesen.ixn.napcore.client.command.capabilities;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.OnboardingCapability;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "get",
        description = "Get one NAP capability",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetNapCapability implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the NAP capability")
    String capabilityId;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        OnboardingCapability capability = client.getCapability(capabilityId);
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capability));
        return 0;
    }
}
