package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.AddPeersRequest;
import picocli.CommandLine.*;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add private channel peers from file",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient privatechannels peers add -f peers.json 43f9ffe7-e8ed-4165-a61c-21f6f2e55a57
                        """
        }
)
public class AddPeersToPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PeersCommand parentCommand;

    @Parameters(index = "0", description = "Id of the private channel to add to")
    String privateChannelId;

    @Option(names = {"-f", "--file"}, required = true)
    File file;


    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        AddPeersRequest request = mapper.readValue(file, AddPeersRequest.class);
        client.addPeersToPrivateChannel(privateChannelId, request);
        return 0;
    }
}
