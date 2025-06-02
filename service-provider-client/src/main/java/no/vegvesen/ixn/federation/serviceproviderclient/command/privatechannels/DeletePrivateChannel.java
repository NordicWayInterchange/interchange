package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(name = "delete", description = "Delete a service provider private channel to a client",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient privatechannels delete 43f9ffe7-e8ed-4165-a61c-21f6f2e55a57
                        """
        })
public class DeletePrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivateChannelsCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the subscription to delete")
    String privateChannelId;

    @Override
    public Integer call() {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        client.deletePrivateChannel(privateChannelId);
        System.out.printf("Private channel with id %s deleted successfully%n", privateChannelId);
        return 0;
    }
}
