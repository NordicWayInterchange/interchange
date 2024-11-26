package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
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
    PeersCommand parentCommand;

    @Parameters(index = "0", description = "Id of the private channel to remove yourself from")
    String privateChannelId;


    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().getParent().createClient();
        client.peerDeletePeerFromPrivateChannel(privateChannelId);
        System.out.printf("Successfully deleted you from private channel with id %s", privateChannelId);
        return 0;
    }
}
