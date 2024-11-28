package no.vegvesen.ixn.napcore.client.command.privatechannels.peers;

import no.vegvesen.ixn.napcore.client.command.privatechannels.PrivatechannelsCommand;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;

@CommandLine.Command(
        name="peers",
        description = "Get, add and delete peers from private channels you own. Remove yourself from other private channels",
        subcommands = {
                GetPeerPrivateChannels.class,
                AddPeerToPrivateChannel.class,
                DeletePeerFromPrivateChannel.class,
                PeerDeletePeerFromPrivateChannel.class
        }
)
public class PeersCommand {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    public PrivatechannelsCommand getParent(){
        return parentCommand;
    }
}
