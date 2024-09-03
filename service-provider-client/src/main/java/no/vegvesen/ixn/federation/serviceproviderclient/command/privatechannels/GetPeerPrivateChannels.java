package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.ListPeerPrivateChannels;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(name = "peer", description = "Get all private channels with service provider as peer")
public class GetPeerPrivateChannels implements Callable<Integer> {

    @ParentCommand
    PrivateChannelsCommand parentCommand;


    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        ListPeerPrivateChannels result = client.getPeerPrivateChannels();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}