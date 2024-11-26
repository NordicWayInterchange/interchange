package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.PrivateChannelsCommand;
import picocli.CommandLine.*;

@Command(
        name="peers",
        description = "Get, add and delete peers from private channels you own. Remove yourself from other private channels",
        subcommands = {
                GetPeerPrivateChannels.class,
                AddPeersToPrivateChannel.class,
                DeletePeerFromPrivateChannel.class,
                PeerDeletePeerFromPrivateChannel.class
        }
)
public class PeersCommand {

    @ParentCommand
    PrivateChannelsCommand parentCommand;

    public PrivateChannelsCommand getParent(){
        return parentCommand;
    }
}
