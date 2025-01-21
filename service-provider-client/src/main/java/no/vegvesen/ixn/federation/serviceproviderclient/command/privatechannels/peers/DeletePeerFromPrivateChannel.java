package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete peer from private channel",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class DeletePeerFromPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PeersCommand parentCommand;

    @Parameters(index = "0", description = "Id of the private channel to delete peer from")
    String privateChannelId;

    @Parameters(index = "1", description = "Peer to delete from private channel")
    String peerName;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().getParent().createClient();
        client.deletePeerFromPrivateChannel(privateChannelId, peerName);
        System.out.printf("Successfully deleted peer with name %s from private channel with id %s", peerName, privateChannelId);
        return 0;
    }
}
