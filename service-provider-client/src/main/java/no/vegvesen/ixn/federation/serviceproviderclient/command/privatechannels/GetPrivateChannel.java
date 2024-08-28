package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.GetPrivateChannelResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "get", description = "Get private channel by id")
public class GetPrivateChannel implements Callable<Integer> {
    @ParentCommand
    PrivateChannelsCommand parentCommand;

    @CommandLine.Parameters(index = "0", description = "The ID of the private channel to get")
    String privateChannelId;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        GetPrivateChannelResponse result = client.getPrivateChannel(privateChannelId);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}

