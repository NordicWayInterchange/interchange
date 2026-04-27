package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete peer from private channel",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient privatechannels peers delete 43f9ffe7-e8ed-4165-a61c-21f6f2e55a57 king_olav
                        """
        }
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
        return 0;
    }
}
