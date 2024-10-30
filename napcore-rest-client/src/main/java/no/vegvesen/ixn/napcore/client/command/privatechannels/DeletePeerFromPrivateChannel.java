package no.vegvesen.ixn.napcore.client.command.privatechannels;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "deletepeer",
        description = "Delete peer from private channel",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeletePeerFromPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    @Parameters(index = "0", description = "The id of the private channel")
    String privateChannelId;

    @Parameters(index = "1", description = "The id of the peer to delete")
    String peerName;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        client.deletePeerFromPrivateChannel(privateChannelId, peerName);
        System.out.printf("Successfully deleted peer with id %s from private channel with id %s", peerName, privateChannelId);
        return 0;
    }

}
