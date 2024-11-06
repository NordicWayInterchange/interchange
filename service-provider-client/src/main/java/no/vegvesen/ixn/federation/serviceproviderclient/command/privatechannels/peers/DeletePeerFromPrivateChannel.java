package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.DeletePeerRequest;
import picocli.CommandLine.*;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete peer from private channel",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeletePeerFromPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PeersCommand parentCommand;

    @Parameters(index = "0", description = "Id of the private channel to delete peer from")
    String privateChannelId;

    @Option(names = {"-f", "--filename"})
    File file;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        DeletePeerRequest request = mapper.readValue(file, DeletePeerRequest.class);
        client.deletePeerFromPrivateChannel(privateChannelId, request);
        System.out.printf("Successfully deleted peer with name %s from private channel with id %s", request.getPeerName(), privateChannelId);
        return 0;
    }
}
