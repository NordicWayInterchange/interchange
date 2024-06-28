package no.vegvesen.ixn.napcore.client.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import static picocli.CommandLine.*;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete nap capability",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeleteNapCapability implements Callable<Integer> {


    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the NAP capability")
    String capabilityId;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        client.deleteCapability(capabilityId);
        System.out.printf("Nap capability with id %s deleted successfully", capabilityId);
        return 0;
    }

}
