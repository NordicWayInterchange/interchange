package no.vegvesen.ixn.napcore.client.command.privatechannels.peers;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.util.concurrent.Callable;

@Command(
        name = "deletefrom",
        description = "Delete yourself from private channel",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class PeerDeletePeerFromPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PeersCommand parentCommand;

    @Parameters(index = "0", description = "The id of the private channel")
    String privateChannelId;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParent().getParentCommand().createClient();
        client.peerDeletePeerFromPrivateChannel(privateChannelId);
        System.out.printf("Successfully deleted you from private channel with id %s", privateChannelId);
        return 0;
    }
}
