package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.ListPrivateChannelsResponse;
import picocli.CommandLine.*;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "list", description = "list the private channels of a Service Provider")
public class GetPrivateChannels implements Callable<Integer> {

    @ParentCommand
    PrivateChannelsCommand parentCommand;

    @Override
    public Integer call() throws IOException {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        ListPrivateChannelsResponse result = client.getPrivateChannels();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}