package no.vegvesen.ixn.napcore.client.command.privatechannels;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.PrivateChannelResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.Callable;

@Command(
        name = "get",
        description = "Get one private channel",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    @Parameters(index = "0", description = "The id of the private channel")
    String privateChannelId;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        PrivateChannelResponse privateChannel = client.getPrivateChannel(privateChannelId);
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateChannel));
        return 0;
    }
}
