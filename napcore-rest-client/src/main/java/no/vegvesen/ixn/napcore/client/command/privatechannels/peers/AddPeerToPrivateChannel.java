package no.vegvesen.ixn.napcore.client.command.privatechannels.peers;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.AddPeerRequest;
import picocli.CommandLine.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add private channel peer from file",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddPeerToPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PeersCommand parentCommand;

    @Parameters(index = "0", description = "The id of the private channel")
    String privateChannelId;

    @Option(names = {"-f", "--file"}, required = true)
    File file;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParent().getParentCommand().createClient();
        ObjectMapper mapper = JsonMapper.builder().build();
        AddPeerRequest request = mapper.readValue(file, AddPeerRequest.class);
        client.addPeerToPrivateChannel(privateChannelId, request);
        System.out.printf("successfully added %s to private channel with id %s", request.getPeerToAdd(), privateChannelId);
        return 0;
    }
}
