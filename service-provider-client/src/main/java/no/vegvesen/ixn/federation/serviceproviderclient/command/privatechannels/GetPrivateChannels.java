package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.model.ListPrivateChannelsResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "list", description = "list the private channels of a Service Provider")
public class GetPrivateChannels implements Callable<Integer> {

    @ParentCommand
    PrivateChannelsCommand parentCommand;

    @Override
    public Integer call() throws IOException {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        ListPrivateChannelsResponse result = client.getPrivateChannels();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}