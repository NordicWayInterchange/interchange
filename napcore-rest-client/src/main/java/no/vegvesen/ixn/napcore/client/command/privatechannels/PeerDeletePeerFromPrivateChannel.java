package no.vegvesen.ixn.napcore.client.command.privatechannels;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "deletefrom",
        description = "Delete yourself from private channel",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class PeerDeletePeerFromPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    @Parameters(index = "0", description = "The id of the private channel")
    String privateChannelId;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        client.peerDeletePeerFromPrivateChannel(privateChannelId);
        System.out.printf("Successfully deleted you from private channel with id %s", privateChannelId);
        return 0;
    }
}
