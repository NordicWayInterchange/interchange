package no.vegvesen.ixn.napcore.client.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "getpublicationids",
        description = "Get all publicationIds",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetPublicationIds implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        Set<String> response = client.getPublicationIds();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
