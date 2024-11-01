package no.vegvesen.ixn.napcore.client.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.AddPeerRequest;
import picocli.CommandLine.*;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "addpeer",
        description = "Add private channel peer from file",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddPeerToPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    @Parameters(index = "0", description = "The id of the private channel")
    String privateChannelId;

    @Option(names = {"-f", "--file"}, required = true)
    File file;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = new ObjectMapper();
        AddPeerRequest request = mapper.readValue(file, AddPeerRequest.class);
        client.addPeerToPrivateChannel(privateChannelId, request);
        System.out.printf("successfully added %s to private channel with id %s", request.getPeerToAdd(), privateChannelId);
        return 0;
    }
}
