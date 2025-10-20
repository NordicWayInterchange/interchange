package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.PrivateChannelsCommand;
import picocli.CommandLine.*;

@Command(
        name="peers",
        description = "Listen, get, add and delete peers from private channels you own. Remove yourself from other private channels",
        subcommands = {
                GetPeerPrivateChannels.class,
                AddPeersToPrivateChannel.class,
                DeletePeerFromPrivateChannel.class,
                PeerDeletePeerFromPrivateChannel.class,
                Listen.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class PeersCommand {

    @ParentCommand
    PrivateChannelsCommand parentCommand;

    public PrivateChannelsCommand getParent(){
        return parentCommand;
    }
}
