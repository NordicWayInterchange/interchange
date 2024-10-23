package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.AddPrivateChannelRequest;
import no.vegvesen.ixn.serviceprovider.model.AddPrivateChannelResponse;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelRequestApi;
import picocli.CommandLine.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(name = "add", description = "Adding name for client to set up private channel")
public class AddPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivateChannelsCommand parentCommand;

    @ArgGroup(exclusive = true, multiplicity = "1")
    AddPrivateChannelOption option;

    @Override
    public Integer call() throws IOException {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        if(option.file != null) {
            AddPrivateChannelRequest privateChannel = mapper.readValue(option.file, AddPrivateChannelRequest.class);
            AddPrivateChannelResponse result = client.addPrivateChannel(privateChannel);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        else{
            AddPrivateChannelRequest privateChannel = new AddPrivateChannelRequest(List.of(new PrivateChannelRequestApi(option.peers, option.description)));
            AddPrivateChannelResponse result = client.addPrivateChannel(privateChannel);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        return 0;

    }

    static class AddPrivateChannelOption{
        @Option(names = {"-f", "--filename"}, required = true, description = "The private channel json file")
        File file;

        @Option(names = {"-p", "--peers"}, required = true, description = "The private channel peers")
        Set<String> peers;

        @Option(names = {"-d", "--description"}, required = false, description = "The description of the private channel")
        String description;
    }
}
