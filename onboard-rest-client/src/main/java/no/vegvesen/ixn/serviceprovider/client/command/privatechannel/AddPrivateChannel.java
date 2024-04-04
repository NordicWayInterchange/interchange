package no.vegvesen.ixn.serviceprovider.client.command.privatechannel;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import no.vegvesen.ixn.serviceprovider.model.AddPrivateChannelRequest;
import no.vegvesen.ixn.serviceprovider.model.AddPrivateChannelResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "add", description = "Adding name for client to set up private channel")
public class AddPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivateChannelCommand parentCommand;

    @Option(names = {"-f", "--filename"}, description = "The json file for the peerName")
    File file;

    @Override
    public Integer call() throws IOException {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        AddPrivateChannelRequest privateChannel = mapper.readValue(file, AddPrivateChannelRequest.class);
        AddPrivateChannelResponse result = client.addPrivateChannel(privateChannel);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
