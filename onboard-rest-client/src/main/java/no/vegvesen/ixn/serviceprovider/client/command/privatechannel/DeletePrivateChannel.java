package no.vegvesen.ixn.serviceprovider.client.command.privatechannel;

import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "delete", description = "Delete a service provider private channel to a client")
public class DeletePrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivateChannelCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the subscription to delete")
    String privateChannelId;

    @Override
    public Integer call() {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        client.deletePrivateChannel(privateChannelId);
        System.out.printf("Private channel with id %s deleted successfully%n", privateChannelId);
        return 0;
    }
}
