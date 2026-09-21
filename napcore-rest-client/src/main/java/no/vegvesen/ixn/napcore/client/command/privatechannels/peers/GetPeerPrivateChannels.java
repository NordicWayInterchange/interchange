package no.vegvesen.ixn.napcore.client.command.privatechannels.peers;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.PeerPrivateChannel;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "get",
        description = "List private channels where you are peer",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetPeerPrivateChannels implements Callable<Integer> {

    @ParentCommand
    PeersCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParent().getParentCommand().createClient();
        List<PeerPrivateChannel> response = client.getPeerPrivateChannels();
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
