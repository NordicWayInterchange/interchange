package no.vegvesen.ixn.napcore.client.command.privatechannels;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.PrivateChannelResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "List all private channels",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetPrivateChannels implements Callable<Integer> {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        List<PrivateChannelResponse> response = client.getPrivateChannels();
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
