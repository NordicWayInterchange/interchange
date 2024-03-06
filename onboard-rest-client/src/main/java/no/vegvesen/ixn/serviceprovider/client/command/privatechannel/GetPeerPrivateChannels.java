package no.vegvesen.ixn.serviceprovider.client.command.privatechannel;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import no.vegvesen.ixn.serviceprovider.model.ListPeerPrivateChannels;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "peer", description = "Get all private channels with service provider as peer")
public class GetPeerPrivateChannels implements Callable<Integer> {
    @ParentCommand
    PrivateChannelCommand parentCommand;


    @Override
    public Integer call() throws Exception {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        ListPeerPrivateChannels result = client.getPeerPrivateChannels();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
