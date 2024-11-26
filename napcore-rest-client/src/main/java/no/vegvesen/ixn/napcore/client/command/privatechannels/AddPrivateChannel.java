package no.vegvesen.ixn.napcore.client.command.privatechannels;


import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.PrivateChannelRequest;
import no.vegvesen.ixn.napcore.model.PrivateChannelResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.io.File;
import java.util.concurrent.Callable;


@Command(
        name = "add",
        description = "Add private channel from file",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddPrivateChannel implements Callable<Integer> {

    @ParentCommand
    PrivatechannelsCommand parentCommand;

    @Option(names = {"-f", "--file"}, required = true)
    File file;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = new ObjectMapper();
        PrivateChannelRequest request = mapper.readValue(file, PrivateChannelRequest.class);
        PrivateChannelResponse response = client.addPrivateChannel(request);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
