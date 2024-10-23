package no.vegvesen.ixn.napcore.client.command.privatechannels;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete private channel",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeletePrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    @Parameters(index = "0", description = "The id of the private channel")
    String privateChannelId;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        client.deletePrivateChannel(privateChannelId);
        System.out.printf("Private channel with id %s deleted successfully", privateChannelId);
        return 0;
    }
}
