package no.vegvesen.ixn.napcore.client.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.OnboardingCapability;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "get",
        description = "Get one NAP capability",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetNapCapability implements Callable<Integer> {

    @CommandLine.ParentCommand
    CapabilitiesCommand parentCommand;

    @CommandLine.Parameters(index = "0", description = "The ID of the NAP capability")
    String capabilityId;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        OnboardingCapability capability = client.getCapability(capabilityId);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capability));
        return 0;
    }
}
