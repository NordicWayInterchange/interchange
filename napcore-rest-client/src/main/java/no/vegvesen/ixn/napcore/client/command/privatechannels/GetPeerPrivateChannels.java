package no.vegvesen.ixn.napcore.client.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.PeerPrivateChannel;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "peer",
        description = "List private channels where you are peer",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetPeerPrivateChannels implements Callable<Integer> {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        List<PeerPrivateChannel> response = client.getPeerPrivateChannels();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
